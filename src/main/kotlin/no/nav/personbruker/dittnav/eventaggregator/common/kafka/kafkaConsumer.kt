package no.nav.personbruker.dittnav.eventaggregator.common.kafka

import no.nav.brukernotifikasjon.schemas.Nokkel
import org.apache.kafka.clients.consumer.KafkaConsumer

fun <T> KafkaConsumer<Nokkel, T>.rollbackToLastCommitted() {
    assignment().forEach { partition ->
        val lastCommitted = committed(partition)
        seek(partition, lastCommitted.offset())
    }
}