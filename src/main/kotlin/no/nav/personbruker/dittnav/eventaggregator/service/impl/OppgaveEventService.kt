package no.nav.personbruker.dittnav.eventaggregator.service.impl

import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.database.Database
import no.nav.personbruker.dittnav.eventaggregator.database.entity.createOppgave
import no.nav.personbruker.dittnav.eventaggregator.database.entity.getOppgaveById
import no.nav.personbruker.dittnav.eventaggregator.service.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.transformer.OppgaveTransformer
import org.apache.kafka.clients.consumer.ConsumerRecord

import org.slf4j.LoggerFactory

class OppgaveEventService(
        val database: Database
) : EventBatchProcessorService<Oppgave> {

    val log = LoggerFactory.getLogger(OppgaveEventService::class.java)

    override fun <T> processEvent(event: ConsumerRecord<String, T>) {
        storeEventInCache(event.value() as Oppgave)
    }

    fun storeEventInCache(event: Oppgave) {
        val entity = OppgaveTransformer.toInternal(event)
        log.info("Skal skrive entitet til databasen: $entity")
        runBlocking {
            val entityId = database.dbQuery { createOppgave(entity) }
            val fetchedRow = database.dbQuery { getOppgaveById(entityId) }
            log.info("Ny rad hentet fra databasen $fetchedRow")
        }
    }
}