package no.nav.personbruker.dittnav.eventaggregator.service.impl

import no.nav.brukernotifikasjon.schemas.Done
import no.nav.personbruker.dittnav.eventaggregator.database.Database
import no.nav.personbruker.dittnav.eventaggregator.service.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.transformer.DoneTransformer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DoneEventService(
        val database: Database,
        val transformer: DoneTransformer = DoneTransformer()
) : EventBatchProcessorService<Done> {

    private val log : Logger = LoggerFactory.getLogger(DoneEventService::class.java)

    override fun <T> processEvent(event: ConsumerRecord<String, T>) {
        store(event.value() as Done)
    }

    fun store(event: Done) {
        val entity = transformer.toInternal(event)
        log.info("Fikk følgende Done-event: $entity")
    }

}