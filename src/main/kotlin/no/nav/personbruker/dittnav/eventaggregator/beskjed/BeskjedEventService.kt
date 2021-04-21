package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.brukernotifikasjon.schemas.Beskjed
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.brukernotifikasjon.schemas.builders.exception.FieldValidationException
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonPersistingService
import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.NokkelNullException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.serializer.getNonNullKey
import no.nav.personbruker.dittnav.eventaggregator.config.EventType.BESKJED
import no.nav.personbruker.dittnav.eventaggregator.config.isProdEnvironment
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsSession
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BeskjedEventService(
        private val persistingService: BrukernotifikasjonPersistingService<no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed>,
        private val metricsProbe: EventMetricsProbe
) : EventBatchProcessorService<Beskjed> {

    private val log: Logger = LoggerFactory.getLogger(BeskjedEventService::class.java)

    override suspend fun processEvents(events: ConsumerRecords<Nokkel, Beskjed>) {
        val successfullyTransformedEvents = mutableListOf<no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed>()
        val problematicEvents = mutableListOf<ConsumerRecord<Nokkel, Beskjed>>()

        metricsProbe.runWithMetrics(eventType = BESKJED) {
            events.forEach { event ->
                try {
                    val internalEventKey = event.getNonNullKey()
                    val internalEventValue = BeskjedTransformer.toInternal(internalEventKey, event.value())
                    successfullyTransformedEvents.add(internalEventValue)
                    countSuccessfulEventForProducer(internalEventKey.getSystembruker())
                } catch (nne: NokkelNullException) {
                    countFailedEventForProducer("NoProducerSpecified")
                    log.warn("Eventet manglet nøkkel. Topic: ${event.topic()}, Partition: ${event.partition()}, Offset: ${event.offset()}", nne)
                } catch (fve: FieldValidationException) {
                    countFailedEventForProducer(event.systembruker ?: "NoProducerSpecified")
                    val msg = "Klarte ikke transformere eventet pga en valideringsfeil. EventId: ${event.eventId}, systembruker: ${event.systembruker}, $fve"
                    log.warn(msg, fve)
                } catch (e: Exception) {
                    countFailedEventForProducer(event.systembruker ?: "NoProducerSpecified")
                    problematicEvents.add(event)
                    log.warn("Transformasjon av beskjed-event fra Kafka feilet, fullfører batch-en før pollig stoppes.", e)
                }
            }

            val result = persistingService.writeEventsToCache(successfullyTransformedEvents)

            countDuplicateKeyEvents(result)
        }

        kastExceptionHvisMislykkedeTransformasjoner(problematicEvents)
    }

    private fun EventMetricsSession.countDuplicateKeyEvents(result: ListPersistActionResult<no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed>) {
        if (result.foundConflictingKeys()) {

            val constraintErrors = result.getConflictingEntities().size
            val totalEntities = result.getAllEntities().size

            result.getConflictingEntities()
                    .groupingBy { beskjed -> beskjed.systembruker }
                    .eachCount()
                    .forEach { (systembruker, duplicates) ->
                        countDuplicateEventKeysByProducer(systembruker, duplicates)
                    }

            val msg = """Traff $constraintErrors feil på duplikate eventId-er ved behandling av $totalEntities beskjed-eventer.
                           | Feilene ble produsert av: ${getNumberDuplicateKeysByProducer()}""".trimMargin()
            logAsWarningForAllProducersExceptForFpinfoHistorikk(msg)
        }
    }

    private fun kastExceptionHvisMislykkedeTransformasjoner(problematicEvents: MutableList<ConsumerRecord<Nokkel, Beskjed>>) {
        if (problematicEvents.isNotEmpty()) {
            val message = "En eller flere eventer kunne ikke transformeres"
            val exception = UntransformableRecordException(message)
            exception.addContext("antallMislykkedeTransformasjoner", problematicEvents.size)
            throw exception
        }
    }

}
