package no.nav.personbruker.dittnav.eventaggregator.common.kafka

fun <K, V> org.apache.kafka.clients.consumer.Consumer<K, V>.rollbackToLastCommitted() {
    val assignedPartitions = assignment()
    val partitionCommittedInfo = committed(assignedPartitions)
    partitionCommittedInfo.forEach { (partition, lastCommitted) ->
        seek(partition, lastCommitted.offset())
    }
}
