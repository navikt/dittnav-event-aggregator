package no.nav.personbruker.dittnav.eventaggregator.config

data class MetricsState (
        val topic: String,
        val producer: String,
        val messagesSeen: Int,
        val messageLastSeen: Long
)

fun beskjedMetricsState(producer: String, messagesSeen: Int, messageLastSeen: Long): MetricsState {
    return MetricsState(Kafka.beskjedTopicName, producer, messagesSeen, messageLastSeen)
}

fun innboksMetricsState(producer: String, messagesSeen: Int, messageLastSeen: Long): MetricsState {
    return MetricsState(Kafka.innboksTopicName, producer, messagesSeen, messageLastSeen)
}

fun oppgaveMetricsState(producer: String, messagesSeen: Int, messageLastSeen: Long): MetricsState {
    return MetricsState(Kafka.oppgaveTopicName, producer, messagesSeen, messageLastSeen)
}

fun doneMetricsState(producer: String, messagesSeen: Int, messageLastSeen: Long): MetricsState {
    return MetricsState(Kafka.doneTopicName, producer, messagesSeen, messageLastSeen)
}