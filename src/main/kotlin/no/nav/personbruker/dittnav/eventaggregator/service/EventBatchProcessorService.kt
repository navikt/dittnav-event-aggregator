package no.nav.personbruker.dittnav.eventaggregator.service

import org.apache.kafka.clients.consumer.ConsumerRecord

interface EventBatchProcessorService<T> {

    fun <T> processEvent(event: ConsumerRecord<String, T>)

    fun processEvents(batchOfEvents: List<T>)

}