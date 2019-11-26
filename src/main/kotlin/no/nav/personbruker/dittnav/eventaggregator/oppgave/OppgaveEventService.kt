package no.nav.personbruker.dittnav.eventaggregator.oppgave

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistFailureReason
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory

class OppgaveEventService(
        val database: Database
) : EventBatchProcessorService<Oppgave> {

    private val log = LoggerFactory.getLogger(OppgaveEventService::class.java)

    override suspend fun processEvents(events: ConsumerRecords<String, Oppgave>) {
        events.forEach { event ->
            storeEventInCache(event.value() as Oppgave)
        }
    }

    private suspend fun storeEventInCache(event: Oppgave) {
        val entity = OppgaveTransformer.toInternal(event)
        log.info("Skal skrive entitet til databasen: $entity")

        withContext(Dispatchers.IO) {
            database.queryWithExceptionTranslation {
                createOppgave(entity).onSuccess { entityId ->
                    val storedOppgave = getOppgaveById(entityId)
                    log.info("Oppgave hentet i databasen: $storedOppgave")
                }.onFailure { reason ->
                    when (reason) {
                        PersistFailureReason.CONFLICTING_KEYS ->
                            log.warn("Hoppet over persistering av Oppgave fordi produsent tidligere har brukt samme eventId: $entity")
                        else ->
                            log.warn("Hoppet over persistering av Oppgave: $entity")
                    }

                }
            }
        }
    }
}
