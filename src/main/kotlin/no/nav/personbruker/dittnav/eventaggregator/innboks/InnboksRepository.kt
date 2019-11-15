package no.nav.personbruker.dittnav.eventaggregator.innboks

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import org.slf4j.LoggerFactory

class InnboksRepository(private val database: Database) {

    val log = LoggerFactory.getLogger(InnboksRepository::class.java)

    suspend fun storeInnboksEventInCache(innboks: Innboks) {
        database.translateExternalExceptionsToInternalOnes {
            runBlocking {
                database.dbQuery {
                    val entityId = createInnboks(innboks)
                    val storedInnboks = getInnboksById(entityId)
                    log.info("Innboks hentet i databasen: $storedInnboks")
                }
            }
        }
    }
}