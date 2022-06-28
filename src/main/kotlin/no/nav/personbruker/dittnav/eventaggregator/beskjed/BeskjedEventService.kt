package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.brukernotifikasjon.schemas.internal.BeskjedIntern
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonPersistingService
import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.config.EventType.BESKJED_INTERN
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsSession
import no.nav.personbruker.dittnav.eventaggregator.metrics.Produsent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BeskjedEventService(
        private val persistingService: BrukernotifikasjonPersistingService<Beskjed>,
        private val metricsProbe: EventMetricsProbe
) : EventBatchProcessorService<NokkelIntern, BeskjedIntern> {

    private val log: Logger = LoggerFactory.getLogger(BeskjedEventService::class.java)

    override suspend fun processEvents(events: ConsumerRecords<NokkelIntern, BeskjedIntern>) {
        val successfullyTransformedEvents = mutableListOf<Beskjed>()
        val problematicEvents = mutableListOf<ConsumerRecord<NokkelIntern, BeskjedIntern>>()

        metricsProbe.runWithMetrics(eventType = BESKJED_INTERN) {
            events.forEach { event ->
                try {
                    val internalEventValue = BeskjedTransformer.toInternal(event.key(), event.value())
                    successfullyTransformedEvents.add(internalEventValue)
                    countSuccessfulEventForProducer(Produsent(internalEventValue.appnavn, internalEventValue.namespace))
                } catch (e: Exception) {
                    countFailedEventForProducer(Produsent(event.appnavn, event.namespace))
                    problematicEvents.add(event)
                    log.warn("Transformasjon av beskjed-event fra Kafka feilet, fullfører batch-en før pollig stoppes.", e)
                }
            }

            val result = persistingService.writeEventsToCache(successfullyTransformedEvents)

            countDuplicateKeyEvents(result)
        }

        kastExceptionHvisMislykkedeTransformasjoner(problematicEvents)
    }

    private fun EventMetricsSession.countDuplicateKeyEvents(result: ListPersistActionResult<Beskjed>) {
        if (result.foundUnalteredEntitites()) {

            val constraintErrors = result.getUnalteredEntities().size
            val totalEntities = result.getAllEntities().size

            result.getUnalteredEntities()
                    .groupingBy { beskjed -> Produsent(beskjed.appnavn, beskjed.namespace) }
                    .eachCount()
                    .forEach { (produsent, duplicates) ->
                        countDuplicateEventKeysByProducer(produsent, duplicates)
                    }

            val msg = """Traff $constraintErrors feil på duplikate eventId-er ved behandling av $totalEntities beskjed-eventer.
                           | Feilene ble produsert av: ${getNumberDuplicateKeysByProducer()}""".trimMargin()
            logAsWarningForAllProducersExceptForFpinfoHistorikk(msg)
        }
    }

    private fun kastExceptionHvisMislykkedeTransformasjoner(problematicEvents: MutableList<ConsumerRecord<NokkelIntern, BeskjedIntern>>) {
        if (problematicEvents.isNotEmpty()) {
            val message = "En eller flere eventer kunne ikke transformeres"
            val exception = UntransformableRecordException(message)
            exception.addContext("antallMislykkedeTransformasjoner", problematicEvents.size)
            throw exception
        }
    }

}
