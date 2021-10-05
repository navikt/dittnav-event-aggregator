package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.brukernotifikasjon.schemas.internal.StatusoppdateringIntern
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonPersistingService
import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.config.EventType.STATUSOPPDATERING_INTERN
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsSession
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class StatusoppdateringEventService(
        private val persistingService: BrukernotifikasjonPersistingService<Statusoppdatering>,
        private val metricsProbe: EventMetricsProbe
) : EventBatchProcessorService<StatusoppdateringIntern> {

    private val log: Logger = LoggerFactory.getLogger(StatusoppdateringEventService::class.java)

    override suspend fun processEvents(events: ConsumerRecords<NokkelIntern, StatusoppdateringIntern>) {
        val successfullyTransformedEvents = mutableListOf<Statusoppdatering>()
        val problematicEvents = mutableListOf<ConsumerRecord<NokkelIntern, StatusoppdateringIntern>>()

        metricsProbe.runWithMetrics(eventType = STATUSOPPDATERING_INTERN) {
            events.forEach { event ->
                try {
                    val internalEventValue = StatusoppdateringTransformer.toInternal(event.key(), event.value())
                    successfullyTransformedEvents.add(internalEventValue)
                    countSuccessfulEventForProducer(internalEventValue.appnavn)
                } catch (e: Exception) {
                    countFailedEventForProducer(event.appnavn)
                    problematicEvents.add(event)
                    log.warn("Transformasjon av statusoppdaterings-event fra Kafka feilet, fullfører batch-en før pollig stoppes.", e)
                }
            }

            val result = persistingService.writeEventsToCache(successfullyTransformedEvents)

            countDuplicateKeyEvents(result)
        }

        kastExceptionHvisMislykkedeTransformasjoner(problematicEvents)
    }

    private fun EventMetricsSession.countDuplicateKeyEvents(result: ListPersistActionResult<Statusoppdatering>) {
        if (result.foundConflictingKeys()) {

            val constraintErrors = result.getConflictingEntities().size
            val totalEntities = result.getAllEntities().size

            result.getConflictingEntities()
                    .groupingBy { statusoppdatering -> statusoppdatering.appnavn }
                    .eachCount()
                    .forEach { (appnavn, duplicates) ->
                        countDuplicateEventKeysByProducer(appnavn, duplicates)
                    }

            val msg = """Traff $constraintErrors feil på duplikate eventId-er ved behandling av $totalEntities statusoppdaterings-eventer.
                           | Feilene ble produsert av: ${getNumberDuplicateKeysByProducer()}""".trimMargin()
            logAsWarningForAllProducersExceptForFpinfoHistorikk(msg)
        }
    }

    private fun kastExceptionHvisMislykkedeTransformasjoner(problematicEvents: MutableList<ConsumerRecord<NokkelIntern, StatusoppdateringIntern>>) {
        if (problematicEvents.isNotEmpty()) {
            val message = "En eller flere eventer kunne ikke transformeres"
            val exception = UntransformableRecordException(message)
            exception.addContext("antallMislykkedeTransformasjoner", problematicEvents.size)
            throw exception
        }
    }
}
