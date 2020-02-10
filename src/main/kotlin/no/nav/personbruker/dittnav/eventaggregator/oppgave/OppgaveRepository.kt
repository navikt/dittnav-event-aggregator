package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistFailureReason
import org.slf4j.LoggerFactory

class OppgaveRepository(private val database: Database) {

    val log = LoggerFactory.getLogger(OppgaveRepository::class.java)

    suspend fun storeOppgaveEventInCache(oppgave: Oppgave) {
        database.queryWithExceptionTranslation {
            createOppgave(oppgave).onSuccess { oppgaveId ->
                val storedOppgave = getOppgaveById(oppgaveId)
                log.info("Oppgave hentet i databasen: $storedOppgave")
            }.onFailure { reason ->
                when (reason) {
                    PersistFailureReason.CONFLICTING_KEYS ->
                        log.warn("Hoppet over persistering av Oppgave fordi produsent tidligere har brukt samme eventId: $oppgave")
                    else ->
                        log.warn("Hoppet over persistering av Oppgave: $oppgave")
                }

            }
        }
    }
}