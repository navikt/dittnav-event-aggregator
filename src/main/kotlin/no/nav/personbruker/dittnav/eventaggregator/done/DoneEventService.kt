package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.brukernotifikasjon.schemas.internal.DoneIntern
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.config.EventType.DONE
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsSession
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DoneEventService(
        private val donePersistingService: DonePersistingService,
        private val eventMetricsProbe: EventMetricsProbe
) : EventBatchProcessorService<DoneIntern> {

    private val log: Logger = LoggerFactory.getLogger(DoneEventService::class.java)

    override suspend fun processEvents(events: ConsumerRecords<NokkelIntern, DoneIntern>) {
        val successfullyTransformedEvents = mutableListOf<Done>()
        val problematicEvents = mutableListOf<ConsumerRecord<NokkelIntern, DoneIntern>>()

        eventMetricsProbe.runWithMetrics(eventType = DONE) {

            events.forEach { event ->
                try {
                    val internalEventValue = DoneTransformer.toInternal(event.key(), event.value())
                    successfullyTransformedEvents.add(internalEventValue)
                    countSuccessfulEventForProducer(internalEventValue.systembruker)
                } catch (cce: ClassCastException) {
                    countFailedEventForProducer(event.systembruker)
                    val funnetType = event.javaClass.name
                    val eventId = event.eventId
                    val systembruker = event.systembruker
                    log.warn("Feil eventtype funnet på topic, fant et event av typen $funnetType. Eventet blir forkastet. EventId: $eventId, systembruker: $systembruker, $cce", cce)
                } catch (e: Exception) {
                    countFailedEventForProducer(event.systembruker)
                    problematicEvents.add(event)
                    log.warn("Transformasjon av done-event fra Kafka feilet.", e)
                }
            }
            val groupedDoneEvents = groupDoneEventsByAssociatedEventType(successfullyTransformedEvents)
            donePersistingService.writeDoneEventsForBeskjedToCache(groupedDoneEvents.foundBeskjed)
            donePersistingService.writeDoneEventsForOppgaveToCache(groupedDoneEvents.foundOppgave)
            donePersistingService.writeDoneEventsForInnboksToCache(groupedDoneEvents.foundInnboks)
            val writeEventsToCacheResult = donePersistingService.writeEventsToCache(groupedDoneEvents.notFoundEvents)
            countDuplicateKeyEvents(writeEventsToCacheResult)
        }

        kastExceptionHvisMislykkedeTransformasjoner(problematicEvents)
    }

    private suspend fun groupDoneEventsByAssociatedEventType(successfullyTransformedEvents: List<Done>): DoneBatchProcessor {
        val eventIds = successfullyTransformedEvents.map { it.eventId }.distinct()
        val aktiveBrukernotifikasjoner = donePersistingService.fetchBrukernotifikasjonerFromViewForEventIds(eventIds)
        val batch = DoneBatchProcessor(aktiveBrukernotifikasjoner)
        batch.process(successfullyTransformedEvents)
        return batch
    }

    private fun EventMetricsSession.countDuplicateKeyEvents(result: ListPersistActionResult<Done>) {
        if (result.foundConflictingKeys()) {

            val constraintErrors = result.getConflictingEntities().size
            val totalEntities = result.getAllEntities().size

            result.getConflictingEntities()
                    .groupingBy { done -> done.systembruker }
                    .eachCount()
                    .forEach { (systembruker, duplicates) ->
                        countDuplicateEventKeysByProducer(systembruker, duplicates)
                    }

            val msg = """Traff $constraintErrors feil på duplikate eventId-er ved behandling av $totalEntities done-eventer.
                           | Feilene ble produsert av: ${getNumberDuplicateKeysByProducer()}""".trimMargin()
            logAsWarningForAllProducersExceptForFpinfoHistorikk(msg)
        }
    }

    private fun kastExceptionHvisMislykkedeTransformasjoner(problematicEvents: MutableList<ConsumerRecord<NokkelIntern, DoneIntern>>) {
        if (problematicEvents.isNotEmpty()) {
            val message = "En eller flere eventer kunne ikke transformeres"
            val exception = UntransformableRecordException(message)
            exception.addContext("antallMislykkedeTransformasjoner", problematicEvents.size)
            throw exception
        }
    }
}
