package no.nav.personbruker.dittnav.eventaggregator.service.impl

import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Informasjon
import no.nav.personbruker.dittnav.eventaggregator.database.Database
import no.nav.personbruker.dittnav.eventaggregator.database.entity.createInformasjon
import no.nav.personbruker.dittnav.eventaggregator.database.entity.getInformasjonById
import no.nav.personbruker.dittnav.eventaggregator.service.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.transformer.InformasjonTransformer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class InformasjonEventService(
        val database: Database
) : EventBatchProcessorService<Informasjon> {

    private val log : Logger = LoggerFactory.getLogger(InformasjonEventService::class.java)

    override fun <T> processEvent(event: ConsumerRecord<String, T>) {
        storeEventInCache(event.value() as Informasjon)
    }

    private fun storeEventInCache(event: Informasjon) {
        val entity = InformasjonTransformer.toInternal(event)
        log.info("Skal skrive entitet til databasen: $entity")
        runBlocking {
            val entityID = database.dbQuery { createInformasjon(entity) }
            val fetchedRow = database.dbQuery { getInformasjonById(entityID) }
            log.info("Ny rad hentet fra databasen: $fetchedRow")
        }
    }

}
