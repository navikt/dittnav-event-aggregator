package no.nav.personbruker.dittnav.eventaggregator.informasjon

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.database.Database
import no.nav.personbruker.dittnav.eventaggregator.database.entity.Informasjon
import no.nav.personbruker.dittnav.eventaggregator.database.entity.createInformasjon

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