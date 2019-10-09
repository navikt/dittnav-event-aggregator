package no.nav.personbruker.dittnav.eventaggregator.service.impl

import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Done
import no.nav.personbruker.dittnav.eventaggregator.config.INFORMASJON
import no.nav.personbruker.dittnav.eventaggregator.config.OPPGAVE
import no.nav.personbruker.dittnav.eventaggregator.database.Database
import no.nav.personbruker.dittnav.eventaggregator.database.entity.*
import no.nav.personbruker.dittnav.eventaggregator.service.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.transformer.DoneTransformer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DoneEventService(
        val database: Database
) : EventBatchProcessorService<Done> {

    private val log : Logger = LoggerFactory.getLogger(DoneEventService::class.java)

    override fun <T> processEvent(event: ConsumerRecord<String, T>) {
        processDoneEvent(event.value() as Done)
    }

    private fun processDoneEvent(event: Done) {
        val entity = DoneTransformer.toInternal(event)
        runBlocking {
            val brukernotifikasjoner = database.dbQuery { getAllBrukernotifikasjonerFromView() }
            val foundEvent: Brukernotifikasjon? = brukernotifikasjoner.find { it.id == entity.eventId }
            if (foundEvent != null) {
                log.info("Fant matchende event for Done-event med ${foundEvent.id}")
                setEventActive(foundEvent)
            } else {
                database.dbQuery { createDone(entity) }
                log.info("Fant ikke matchende event for done-event med id ${entity.eventId}. Skrev done-event til cache")
            }
        }
    }

    private suspend fun setEventActive(event: Brukernotifikasjon) {
            when (event.type.toLowerCase()) {
                OPPGAVE -> {
                    database.dbQuery { setOppgaveAktiv(event.id, false) }
                    log.info("Satte Oppgave-event med eventId ${event.id} inaktivt")
                }
                INFORMASJON -> {
                    database.dbQuery{ setInformasjonAktiv(event.id, false) }
                    log.info("Satte Informasjon-event med eventId ${event.id} inaktivt")
                }
            }
    }
}
