package no.nav.personbruker.dittnav.eventaggregator.metrics

import io.prometheus.client.Counter
import io.prometheus.client.Gauge

object PrometheusMetricsCollector {

    val NAMESPACE = "dittnav_consumer"

    val MESSAGES_SEEN_NAME = "kafka_messages_seen"
    val MESSAGES_PROCESSED_NAME = "kafka_messages_processed"
    val MESSAGES_FAILED_NAME = "kafka_messages_failed"
    val MESSAGE_LAST_SEEN_NAME = "kafka_message_type_last_seen"

    private val MESSAGES_SEEN: Counter = Counter.build()
            .name(MESSAGES_SEEN_NAME)
            .namespace(NAMESPACE)
            .help("Messages read since last startup")
            .labelNames("topic", "producer")
            .register()

    private val MESSAGES_PROCESSED: Counter = Counter.build()
            .name(MESSAGES_PROCESSED_NAME)
            .namespace(NAMESPACE)
            .help("Messages successfully processed since last startup")
            .labelNames("topic", "producer")
            .register()

    private val MESSAGES_FAILED: Counter = Counter.build()
            .name(MESSAGES_FAILED_NAME)
            .namespace(NAMESPACE)
            .help("Messages failed since last startup")
            .labelNames("topic", "producer")
            .register()

    private val MESSAGE_LAST_SEEN: Gauge = Gauge.build()
            .name(MESSAGE_LAST_SEEN_NAME)
            .namespace(NAMESPACE)
            .help("Last time topic was seen")
            .labelNames("topic", "producer")
            .register()

    fun registerMessageSeen(topic: String, producer: String) {
        MESSAGES_SEEN.labels(topic, producer).inc()
        MESSAGE_LAST_SEEN.labels(topic, producer).setToCurrentTime()
    }

    fun registerMessageProcessed(topic: String, producer: String) {
        MESSAGES_PROCESSED.labels(topic, producer).inc()
    }

    fun registerMessageFailed(topic: String, producer: String) {
        MESSAGES_FAILED.labels(topic, producer).inc()
    }
}