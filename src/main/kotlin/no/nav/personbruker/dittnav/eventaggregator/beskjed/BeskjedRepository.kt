package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BeskjedRepository(private val database: Database) {

    val log: Logger = LoggerFactory.getLogger(InformasjonRepository::class.java)

    fun writeEventsToCache(entities: List<Beskjed>) {
        database.queryWithExceptionTranslation {
            entities.forEach { entity ->
                val entityId = createBeskjed(entity)

                if (entityId == null) {
                    log.warn("Hoppet over persistering av Beskjed: $entity")
                }
            }
        }
    }

}