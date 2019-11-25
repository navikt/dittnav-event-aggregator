package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import org.slf4j.LoggerFactory

class InnboksRepository(private val database: Database) {

    val log = LoggerFactory.getLogger(InnboksRepository::class.java)

    fun storeInnboksEventInCache(innboks: Innboks) {
        database.queryWithExceptionTranslation {
            createInnboks(innboks)?.let { entityId ->
                val storedInnboks = getInnboksById(entityId)
                log.info("Innboks hentet i databasen: $storedInnboks")
            }?: run {
                log.warn("Hoppet over persistering av  Innboks: $innboks")
            }
        }
    }
}