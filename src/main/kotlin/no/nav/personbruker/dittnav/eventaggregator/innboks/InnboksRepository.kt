package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistFailureReason
import org.slf4j.LoggerFactory

class InnboksRepository(private val database: Database) {

    val log = LoggerFactory.getLogger(InnboksRepository::class.java)

    suspend fun writeEventsToCache(entities: List<Innboks>) {
        database.queryWithExceptionTranslation {
            entities.forEach { entity ->
                createInnboks(entity).onFailure { reason ->
                    when (reason) {
                        PersistFailureReason.CONFLICTING_KEYS ->
                            log.warn("Hoppet over persistering av Innboks fordi produsent tidligere har brukt samme eventId: $entity")
                        else ->
                            log.warn("Hoppet over persistering av Innboks: $entity")
                    }

                }
            }
        }

    }

    suspend fun fetchAll(): List<Innboks> {
        var resultat = emptyList<Innboks>()
        database.queryWithExceptionTranslation {
            resultat = getAllInnboks()
        }
        if (resultat.isEmpty()) {
            log.warn("Fant ingen innboks-eventer i databasen")
        }
        return resultat
    }

    suspend fun fetchActive(): List<Innboks> {
        var resultat = emptyList<Innboks>()
        database.queryWithExceptionTranslation {
            resultat = getAllInnboksByAktiv(true)
        }
        if (resultat.isEmpty()) {
            log.warn("Fant ingen aktive innboks-eventer i databasen")
        }
        return resultat
    }

}
