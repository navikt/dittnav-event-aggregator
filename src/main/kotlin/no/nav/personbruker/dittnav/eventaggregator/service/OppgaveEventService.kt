package no.nav.personbruker.dittnav.eventaggregator.service

import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.database.Database
import no.nav.personbruker.dittnav.eventaggregator.database.entity.createOppgave
import no.nav.personbruker.dittnav.eventaggregator.database.entity.getOppgaveById
import no.nav.personbruker.dittnav.eventaggregator.kafka.Consumer
import no.nav.personbruker.dittnav.eventaggregator.transformer.OppgaveTransformer

import org.slf4j.LoggerFactory

class OppgaveEventService(
        val database: Database,
        val transformer: OppgaveTransformer = OppgaveTransformer()
) {
    val log = LoggerFactory.getLogger(OppgaveEventService::class.java)

    fun storeInEventCache(event: Oppgave) {
        val entity = transformer.toInternal(event)
        Consumer.log.info("Skal skrive entitet til databasen: $entity")
        runBlocking {
            val entityId = database.dbQuery { createOppgave(entity) }
            val fetchedRow = database.dbQuery { getOppgaveById(entityId) }
            log.info("Ny rad hentet fra databasen $fetchedRow")
        }
    }
}