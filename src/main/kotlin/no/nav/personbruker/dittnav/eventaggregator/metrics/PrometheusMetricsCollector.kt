package no.nav.personbruker.dittnav.eventaggregator.metrics

import io.prometheus.client.Counter
import io.prometheus.client.Gauge

object PrometheusMetricsCollector {

    val NAMESPACE = "dittnav_consumer"

    val RUNTIME_MESSAGES_SEEN_NAME = "session_kafka_messages_seen"

    private val RUNTIME_MESSAGES_SEEN: Counter = Counter.build()
            .name(RUNTIME_MESSAGES_SEEN_NAME)
            .namespace(NAMESPACE)
            .help("Messages read since last startup")
            .labelNames("topic", "producer")
            .register()

    val LIFETIME_MESSAGES_SEEN_NAME = "all_time_kafka_messages_seen"

    private val LIFETIME_MESSAGES_SEEN: Counter = Counter.build()
            .name(LIFETIME_MESSAGES_SEEN_NAME)
            .namespace(NAMESPACE)
            .help("Messages read since last startup")
            .labelNames("topic", "producer")
            .register()

    val MESSAGE_LAST_SEEN_NAME = "kafka_topic_last_seen"

    private val MESSAGE_LAST_SEEN: Gauge = Gauge.build()
            .name(MESSAGE_LAST_SEEN_NAME)
            .namespace(NAMESPACE)
            .help("Last time topic was seen")
            .labelNames("topic", "producer")
            .register()

    fun registerMessageSeen(topic: String, producer: String) {
        RUNTIME_MESSAGES_SEEN.labels(topic, producer).inc()
        LIFETIME_MESSAGES_SEEN.labels(topic, producer).inc()
        MESSAGE_LAST_SEEN.labels(topic, producer).setToCurrentTime()
    }
}