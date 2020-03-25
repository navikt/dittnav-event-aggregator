package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.NokkelNullException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.serializer.getNonNullKey
import no.nav.personbruker.dittnav.eventaggregator.config.EventType.OPPGAVE
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory

class OppgaveEventService(
        private val oppgaveRepository: OppgaveRepository,
        private val metricsProbe: EventMetricsProbe
) : EventBatchProcessorService<Oppgave> {

    private val log = LoggerFactory.getLogger(OppgaveEventService::class.java)

    override suspend fun processEvents(events: ConsumerRecords<Nokkel, Oppgave>) {
        val successfullyTransformedEvents = mutableListOf<no.nav.personbruker.dittnav.eventaggregator.oppgave.Oppgave>()
        val problematicEvents = mutableListOf<ConsumerRecord<Nokkel, Oppgave>>()
        events.forEach { event ->
            try {
                metricsProbe.reportEventSeen(OPPGAVE, event.systembruker)
                val internalEvent = OppgaveTransformer.toInternal(event.getNonNullKey(), event.value())
                successfullyTransformedEvents.add(internalEvent)
                metricsProbe.reportEventProcessed(OPPGAVE, event.systembruker)

            } catch (e: NokkelNullException) {
                metricsProbe.reportEventFailed(OPPGAVE, event.systembruker)
                problematicEvents.add(event)
                log.warn("Eventet manglet nøkkel. Topic: ${event.topic()}, Partition: ${event.partition()}, Offset: ${event.offset()}", e)

            } catch (e: Exception) {
                metricsProbe.reportEventFailed(OPPGAVE, event.systembruker)
                problematicEvents.add(event)
                log.warn("Transformasjon av oppgave-event fra Kafka feilet.", e)
            }
        }
        oppgaveRepository.writeEventsToCache(successfullyTransformedEvents)
        kastExceptionHvisMislykkedeTransformasjoner(problematicEvents)
    }

    private fun kastExceptionHvisMislykkedeTransformasjoner(problematicEvents: MutableList<ConsumerRecord<Nokkel, Oppgave>>) {
        if (problematicEvents.isNotEmpty()) {
            val message = "En eller flere eventer kunne ikke transformeres"
            val exception = UntransformableRecordException(message)
            exception.addContext("antallMislykkedeTransformasjoner", problematicEvents.size)
            throw exception
        }
    }

}
