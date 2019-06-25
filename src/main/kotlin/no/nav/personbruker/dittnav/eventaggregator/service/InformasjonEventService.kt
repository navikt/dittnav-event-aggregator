package no.nav.personbruker.dittnav.eventaggregator.service

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.database.repository.InformasjonRepository
import no.nav.personbruker.dittnav.eventaggregator.kafka.Consumer
import no.nav.personbruker.dittnav.eventaggregator.transformer.InformasjonTransformer
import no.nav.personbruker.dittnav.skjema.Informasjon
import org.slf4j.LoggerFactory

class InformasjonEventService(
        val transformer: InformasjonTransformer = InformasjonTransformer(),
        val repository: InformasjonRepository = InformasjonRepository()
) {

    val log = LoggerFactory.getLogger(InformasjonEventService::class.java)

    fun storeEventInCache(event: Informasjon) {
        val entity = transformer.toInternal(event)
        Consumer.log.info("Skal skrive entitet til db: $entity")
        runBlocking {
            val entityID = repository.createInfo(entity)
            val fetchedRow = repository.getInformasjonById(entityID)
            log.info("Ny rad hetet fra databasen: $fetchedRow")
        }
    }

}
