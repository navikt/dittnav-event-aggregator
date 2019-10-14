package no.nav.personbruker.dittnav.eventaggregator.common

import org.apache.kafka.clients.consumer.ConsumerRecords

interface EventBatchProcessorService<T> {

    suspend fun processEvents(events: ConsumerRecords<String, T>)

}