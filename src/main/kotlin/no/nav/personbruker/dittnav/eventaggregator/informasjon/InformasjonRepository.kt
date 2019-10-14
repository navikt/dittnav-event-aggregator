package no.nav.personbruker.dittnav.eventaggregator.informasjon

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database

class InformasjonRepository(private val database: Database) {

    suspend fun writeEventsToCache(entities: List<Informasjon>) {
        database.translateExternalExceptionsToInternalOnes {
            runBlocking {
                database.dbQuery {
                    entities.forEach { entity ->
                        createInformasjon(entity)
                    }
                }
            }
        }
    }

}