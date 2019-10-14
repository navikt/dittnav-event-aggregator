package no.nav.personbruker.dittnav.eventaggregator.done

import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Done
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.Brukernotifikasjon
import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.getAllBrukernotifikasjonFromView
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.informasjon.setInformasjonAktiv
import no.nav.personbruker.dittnav.eventaggregator.oppgave.setOppgaveAktiv
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DoneEventService(
        val database: Database
) : EventBatchProcessorService<Done> {

    private val log: Logger = LoggerFactory.getLogger(DoneEventService::class.java)

    override suspend fun processEvents(events: ConsumerRecords<String, Done>) {
        events.forEach { event ->
            processDoneEvent(event.value() as Done)
        }
    }

    private fun processDoneEvent(event: Done) {
        val entity = DoneTransformer.toInternal(event)
        runBlocking {
            val brukernotifikasjoner = database.dbQuery { getAllBrukernotifikasjonFromView() }
            val foundEvent: Brukernotifikasjon? = brukernotifikasjoner.find { it.id == entity.eventId }
            if (foundEvent != null) {
                log.info("Fant matchende event for Done-event med ${foundEvent.id}")
                setEventActive(foundEvent)
            } else {
                database.dbQuery { createDoneEvent(entity) }
                log.info("Fant ikke matchende event for done-event med id ${entity.eventId}. Skrev done-event til cache")
            }
        }
    }

    private suspend fun setEventActive(event: Brukernotifikasjon) {
        when (event.type) {
            EventType.OPPGAVE -> {
                database.dbQuery { setOppgaveAktiv(event.id, false) }
                log.info("Satte Oppgave-event med eventId ${event.id} inaktivt")
            }
            EventType.INFORMASJON -> {
                database.dbQuery { setInformasjonAktiv(event.id, false) }
                log.info("Satte Informasjon-event med eventId ${event.id} inaktivt")
            }
        }
    }
}
