package no.nav.personbruker.dittnav.eventaggregator.beskjed

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database

class BeskjedRepository(private val database: Database) {

    suspend fun writeEventsToCache(entities: List<Beskjed>) {
        database.translateExternalExceptionsToInternalOnes {
            runBlocking {
                database.dbQuery {
                    entities.forEach { entity ->
                        createBeskjed(entity)
                    }
                }
            }
        }
    }

}