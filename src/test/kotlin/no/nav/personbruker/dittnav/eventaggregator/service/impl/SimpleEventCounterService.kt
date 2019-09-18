package no.nav.personbruker.dittnav.eventaggregator.service.impl

import no.nav.personbruker.dittnav.eventaggregator.service.EventBatchProcessorService
import org.apache.kafka.clients.consumer.ConsumerRecord

class SimpleEventCounterService<T> (var eventCounter: Int = 0) : EventBatchProcessorService<T> {

    override fun <T> processEvent(event: ConsumerRecord<String, T>) {
        eventCounter++
    }

}
