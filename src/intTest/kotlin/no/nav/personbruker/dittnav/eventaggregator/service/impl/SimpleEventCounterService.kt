package no.nav.personbruker.dittnav.eventaggregator.service.impl

import no.nav.personbruker.dittnav.eventaggregator.service.EventBatchProcessorService
import org.apache.kafka.clients.consumer.ConsumerRecords

class SimpleEventCounterService<T>(var eventCounter: Int = 0) : EventBatchProcessorService<T> {

    override suspend fun processEvents(events: ConsumerRecords<String, T>) {
        eventCounter += events.count()
    }

}
