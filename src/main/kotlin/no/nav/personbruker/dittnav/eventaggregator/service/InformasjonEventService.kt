package no.nav.personbruker.dittnav.eventaggregator.service

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.event.schemas.Informasjon
import no.nav.personbruker.dittnav.eventaggregator.config.Database
import no.nav.personbruker.dittnav.eventaggregator.database.query.createInfo
import no.nav.personbruker.dittnav.eventaggregator.database.query.getInformasjonById
import no.nav.personbruker.dittnav.eventaggregator.kafka.Consumer
import no.nav.personbruker.dittnav.eventaggregator.transformer.InformasjonTransformer
import org.slf4j.LoggerFactory

class InformasjonEventService(
        val database: Database,
        val transformer: InformasjonTransformer = InformasjonTransformer()
) {

    val log = LoggerFactory.getLogger(InformasjonEventService::class.java)

    fun storeEventInCache(event: Informasjon) {
        val entity = transformer.toInternal(event)
        Consumer.log.info("Skal skrive entitet til databasen: $entity")
        runBlocking {
            val entityID = database.dbQuery { createInfo(entity) }
            val fetchedRow = database.dbQuery { getInformasjonById(entityID) }
            log.info("Ny rad hentet fra databasen: $fetchedRow")
        }
    }

}
