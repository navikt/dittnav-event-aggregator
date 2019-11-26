package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistFailureReason
import org.slf4j.LoggerFactory

class InnboksRepository(private val database: Database) {

    val log = LoggerFactory.getLogger(InnboksRepository::class.java)

    suspend fun storeInnboksEventInCache(innboks: Innboks) {
        database.queryWithExceptionTranslation {
            createInnboks(innboks).onSuccess { entityId ->
                val storedInnboks = getInnboksById(entityId)
                log.info("Innboks hentet i databasen: $storedInnboks")
            }.onFailure { reason ->
                when(reason) {
                    PersistFailureReason.CONFLICTING_KEYS ->
                        log.warn("Hoppet over persistering av Innboks fordi produsent tidligere har brukt samme eventId: $innboks")
                    else ->
                        log.warn("Hoppet over persistering av Innboks: $innboks")
                }

            }
        }
    }
}