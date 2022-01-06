package no.nav.personbruker.dittnav.eventaggregator.common.kafka

import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern

fun <T> org.apache.kafka.clients.consumer.Consumer<NokkelIntern, T>.rollbackToLastCommitted() {
    val assignedPartitions = assignment()
    val partitionCommittedInfo = committed(assignedPartitions)
    partitionCommittedInfo.forEach { (partition, lastCommitted) ->
        seek(partition, lastCommitted.offset())
    }
}
