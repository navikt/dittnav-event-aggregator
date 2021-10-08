package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.brukernotifikasjon.schemas.internal.InnboksIntern
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonPersistingService
import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.config.EventType.INNBOKS_INTERN
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsSession
import no.nav.personbruker.dittnav.eventaggregator.metrics.Produsent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory

class InnboksEventService(
        private val persistingService: BrukernotifikasjonPersistingService<Innboks>,
        private val metricsProbe: EventMetricsProbe
) : EventBatchProcessorService<InnboksIntern> {

    private val log = LoggerFactory.getLogger(InnboksEventService::class.java)

    override suspend fun processEvents(events: ConsumerRecords<NokkelIntern, InnboksIntern>) {
        val successfullyTransformedEvents = mutableListOf<Innboks>()
        val problematicEvents = mutableListOf<ConsumerRecord<NokkelIntern, InnboksIntern>>()

        metricsProbe.runWithMetrics(eventType = INNBOKS_INTERN) {
            events.forEach { event ->
                try {
                    val internalEventValue = InnboksTransformer.toInternal(event.key(), event.value())
                    successfullyTransformedEvents.add(internalEventValue)
                        countSuccessfulEventForProducer(Produsent(internalEventValue.appnavn, internalEventValue.namespace))
                } catch (e: Exception) {
                    countFailedEventForProducer(Produsent(event.appnavn, event.namespace))
                    problematicEvents.add(event)
                    log.warn("Transformasjon av innboks-event fra Kafka feilet, fullfører batch-en før pollig stoppes.", e)
                }
            }

            val result = persistingService.writeEventsToCache(successfullyTransformedEvents)

            countDuplicateKeyEvents(result)
        }

        kastExceptionHvisMislykkedeTransformasjoner(problematicEvents)
    }

    private fun EventMetricsSession.countDuplicateKeyEvents(result: ListPersistActionResult<Innboks>) {
        if (result.foundConflictingKeys()) {

            val constraintErrors = result.getConflictingEntities().size
            val totalEntities = result.getAllEntities().size

            result.getConflictingEntities()
                    .groupingBy { innboks -> innboks.appnavn }
                    .eachCount()
                    .forEach { (appnavn, duplicates) ->
                        countDuplicateEventKeysByProducer(appnavn, duplicates)
                    }

            val msg = """Traff $constraintErrors feil på duplikate eventId-er ved behandling av $totalEntities innboks-eventer.
                           | Feilene ble produsert av: ${getNumberDuplicateKeysByProducer()}""".trimMargin()
            logAsWarningForAllProducersExceptForFpinfoHistorikk(msg)
        }
    }

    private fun kastExceptionHvisMislykkedeTransformasjoner(problematicEvents: MutableList<ConsumerRecord<NokkelIntern, InnboksIntern>>) {
        if (problematicEvents.isNotEmpty()) {
            val message = "En eller flere eventer kunne ikke transformeres"
            val exception = UntransformableRecordException(message)
            exception.addContext("antallMislykkedeTransformasjoner", problematicEvents.size)
            throw exception
        }
    }
}
