package no.nav.personbruker.dittnav.eventaggregator.common.kafka

import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import org.apache.kafka.clients.consumer.KafkaConsumer

fun <T> KafkaConsumer<NokkelIntern, T>.rollbackToLastCommitted() {
    val assignedPartitions = assignment()
    val partitionCommittedInfo = committed(assignedPartitions)
    partitionCommittedInfo.forEach { (partition, lastCommitted) ->
        seek(partition, lastCommitted.offset())
    }
}
