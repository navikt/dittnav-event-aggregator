package no.nav.personbruker.dittnav.eventaggregator.service

import org.apache.kafka.clients.consumer.ConsumerRecords

interface EventBatchProcessorService<T> {

    suspend fun processEvents(events: ConsumerRecords<String, T>)

}