package no.nav.personbruker.dittnav.eventaggregator.oppgave

import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory

class OppgaveEventService(
        val database: Database
) : EventBatchProcessorService<Oppgave> {

    val log = LoggerFactory.getLogger(OppgaveEventService::class.java)

    override suspend fun processEvents(events: ConsumerRecords<String, Oppgave>) {
        events.forEach { event ->
            storeEventInCache(event.value() as Oppgave)
        }
    }

    private fun storeEventInCache(event: Oppgave) {
        val entity = OppgaveTransformer.toInternal(event)
        log.info("Skal skrive entitet til databasen: $entity")
        runBlocking {
            val entityId = database.dbQuery { createOppgave(entity) }
            val fetchedRow = database.dbQuery { getOppgaveById(entityId) }
            log.info("Ny rad hentet fra databasen $fetchedRow")
        }
    }
}
