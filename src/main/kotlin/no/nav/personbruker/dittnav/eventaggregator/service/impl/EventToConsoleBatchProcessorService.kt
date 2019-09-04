package no.nav.personbruker.dittnav.eventaggregator.service.impl

import no.nav.personbruker.dittnav.eventaggregator.service.EventBatchProcessorService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class EventToConsoleBatchProcessorService<T> : EventBatchProcessorService<T> {

    private val log: Logger = LoggerFactory.getLogger(EventToConsoleBatchProcessorService::class.java)

    override fun <T> processEvent(event: ConsumerRecord<String, T>) {
        log.info("Fant følgende event: $event")
    }

    override fun processEvents(batchOfEvents: List<T>) {
        for (event in batchOfEvents) {
            log.info("Fant følgende event: $event")
        }
    }
}