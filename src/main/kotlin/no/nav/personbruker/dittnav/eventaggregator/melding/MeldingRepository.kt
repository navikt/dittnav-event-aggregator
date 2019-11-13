package no.nav.personbruker.dittnav.eventaggregator.melding

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import org.slf4j.LoggerFactory

class MeldingRepository(private val database: Database) {

    val log = LoggerFactory.getLogger(MeldingRepository::class.java)

    suspend fun storeMeldingEventInCache(melding: Melding) {
        database.translateExternalExceptionsToInternalOnes {
            runBlocking {
                database.dbQuery {
                    val entityId = createMelding(melding)
                    val storedMelding = getMeldingById(entityId)
                    log.info("Melding hentet i databasen: $storedMelding")
                }
            }
        }
    }
}