package no.nav.personbruker.dittnav.eventaggregator.service.impl

import no.nav.personbruker.dittnav.eventaggregator.service.EventBatchProcessorService
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class EventToConsoleBatchProcessorService<T> : EventBatchProcessorService<T> {

    private val log: Logger = LoggerFactory.getLogger(EventToConsoleBatchProcessorService::class.java)

    override suspend fun processEvents(events: ConsumerRecords<String, T>) {
        events.forEach { event ->
            log.info("Fant følgende event: $event")
        }
    }

}
