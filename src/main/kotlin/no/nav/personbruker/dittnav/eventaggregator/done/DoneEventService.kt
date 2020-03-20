package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.NokkelNullException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.serializer.getNonNullKey
import no.nav.personbruker.dittnav.eventaggregator.config.EventType.DONE
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DoneEventService(
        private val doneRepository: DoneRepository,
        private val eventMetricsProbe: EventMetricsProbe
) : EventBatchProcessorService<Done> {

    private val log: Logger = LoggerFactory.getLogger(DoneEventService::class.java)

    override suspend fun processEvents(events: ConsumerRecords<Nokkel, Done>) {
        val successfullyTransformedEvents = mutableListOf<no.nav.personbruker.dittnav.eventaggregator.done.Done>()
        val problematicEvents = mutableListOf<ConsumerRecord<Nokkel, Done>>()
        events.forEach { event ->
            try {
                eventMetricsProbe.reportEventSeen(DONE, event.systembruker)
                val internalEvent = DoneTransformer.toInternal(event.getNonNullKey(), event.value())
                successfullyTransformedEvents.add(internalEvent)
                eventMetricsProbe.reportEventProcessed(DONE, event.systembruker)

            } catch (e: NokkelNullException) {
                eventMetricsProbe.reportEventFailed(DONE, event.systembruker)
                problematicEvents.add(event)
                log.warn("Eventet manglet nøkkel. Topic: ${event.topic()}, Partition: ${event.partition()}, Offset: ${event.offset()}", e)

            } catch (e: Exception) {
                eventMetricsProbe.reportEventFailed(DONE, event.systembruker)
                problematicEvents.add(event)
                log.warn("Transformasjon av done-event fra Kafka feilet.", e)
            }
        }
        val groupedDoneEvents = groupDoneEventsByAssociatedEventType(successfullyTransformedEvents)
        writeDoneEventsToCache(groupedDoneEvents)
        kastExceptionHvisMislykkedeTransformasjoner(problematicEvents)
    }

    private suspend fun groupDoneEventsByAssociatedEventType(successfullyTransformedEvents: MutableList<no.nav.personbruker.dittnav.eventaggregator.done.Done>): DoneBatchProcessor {
        val brukernotifikasjoner = doneRepository.fetchBrukernotifikasjonerFromView()
        val batch = DoneBatchProcessor(brukernotifikasjoner)
        batch.process(successfullyTransformedEvents)
        return batch
    }

    private suspend fun writeDoneEventsToCache(groupedDoneEvents: DoneBatchProcessor) {
        doneRepository.writeDoneEventsForBeskjedToCache(groupedDoneEvents.foundBeskjed)
        doneRepository.writeDoneEventsForOppgaveToCache(groupedDoneEvents.foundOppgave)
        doneRepository.writeDoneEventsForInnboksToCache(groupedDoneEvents.foundInnboks)
        doneRepository.writeDoneEventToCache(groupedDoneEvents.notFoundEvents)
    }

    private fun kastExceptionHvisMislykkedeTransformasjoner(problematicEvents: MutableList<ConsumerRecord<Nokkel, Done>>) {
        if (problematicEvents.isNotEmpty()) {
            val message = "En eller flere eventer kunne ikke transformeres"
            val exception = UntransformableRecordException(message)
            exception.addContext("antallMislykkedeTransformasjoner", problematicEvents.size)
            throw exception
        }
    }

}
