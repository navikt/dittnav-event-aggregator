package no.nav.personbruker.dittnav.eventaggregator.common.kafka

import no.nav.brukernotifikasjon.schemas.Nokkel
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer

fun <T> KafkaConsumer<Nokkel, T>.rollbackToLastCommitted() {
    val assignedPartitions = assignment()
    val partitionCommittedInfo = committed(assignedPartitions)
    partitionCommittedInfo.forEach { (partition, lastCommitted) ->
        seek(partition, lastCommitted.offset())
    }
}

fun <T> KafkaConsumer<Nokkel, T>.resetTheGroupIdsOffsetToZero() {
    assignment().forEach { partition ->
        seek(partition, 0)
    }
}

fun <T> ConsumerRecords<Nokkel, T>.foundRecords(): Boolean {
    return !isEmpty
}
