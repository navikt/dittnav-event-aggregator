package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.NokkelNullException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.serializer.getNonNullKey
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory

class OppgaveEventService(
        val oppgaveRepository: OppgaveRepository
) : EventBatchProcessorService<Oppgave> {

    private val log = LoggerFactory.getLogger(OppgaveEventService::class.java)

    override suspend fun processEvents(events: ConsumerRecords<Nokkel, Oppgave>) {
        val problematicEvents = mutableListOf<ConsumerRecord<Nokkel, Oppgave>>()
        events.forEach { event ->
            try {
                storeEventInCache(event)
            } catch (e: NokkelNullException) {
                log.warn("Eventet manglet nøkkel. Topic: ${event.topic()}, Partition: ${event.partition()}, Offset: ${event.offset()}", e)
            } catch (e: Exception) {
                problematicEvents.add(event)
                log.warn("Transformasjon av oppgave-event fra Kafka feilet.", e)
            }
        }
        kastExceptionHvisMislykkedeTransformasjoner(problematicEvents)
    }

    private suspend fun storeEventInCache(event: ConsumerRecord<Nokkel, Oppgave>) {
        val entity = OppgaveTransformer.toInternal(event.getNonNullKey(), event.value())
        log.info("Skal skrive entitet til databasen: $entity")
        oppgaveRepository.storeOppgaveEventInCache(entity)
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
