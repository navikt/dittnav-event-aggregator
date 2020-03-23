package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistFailureReason
import org.slf4j.LoggerFactory

class OppgaveRepository(private val database: Database) {

    private val log = LoggerFactory.getLogger(OppgaveRepository::class.java)

    suspend fun writeEventsToCache(entities: List<Oppgave>) {
        database.queryWithExceptionTranslation {
            entities.forEach { entity ->
                createOppgave(entity).onFailure { reason ->
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

    suspend fun fetchAll(): List<Oppgave> {
        var resultat = emptyList<Oppgave>()
        database.queryWithExceptionTranslation {
            resultat = getAllOppgave()
        }
        if (resultat.isEmpty()) {
            log.warn("Fant ingen beskjed-eventer i databasen")
        }
        return resultat
    }

    suspend fun fetchActive(): List<Oppgave> {
        var resultat = emptyList<Oppgave>()
        database.queryWithExceptionTranslation {
            resultat = getAllOppgaveByAktiv(true)
        }
        if (resultat.isEmpty()) {
            log.warn("Fant ingen aktive oppgavae-eventer i databasen")
        }
        return resultat
    }

}
