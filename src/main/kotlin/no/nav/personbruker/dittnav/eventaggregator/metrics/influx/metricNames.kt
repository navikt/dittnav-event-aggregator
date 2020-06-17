package no.nav.personbruker.dittnav.eventaggregator.metrics.influx

private const val METRIC_NAMESPACE = "dittnav.kafka.events.v1"

const val KAFKA_EVENTS_SEEN = "$METRIC_NAMESPACE.seen"
const val KAFKA_EVENTS_FAILED = "$METRIC_NAMESPACE.failed"
const val KAFKA_EVENTS_PROCESSED = "$METRIC_NAMESPACE.processed"
const val KAFKA_EVENTS_DUPLICATE_KEY = "$METRIC_NAMESPACE.duplicateKey"
const val KAFKA_EVENTS_BATCH = "$METRIC_NAMESPACE.batch"
const val DB_EVENTS_CACHED = "$METRIC_NAMESPACE.cached"

const val DB_TOTAL_EVENTS_IN_CACHE = "$METRIC_NAMESPACE.db.aggregated.total"
const val DB_TOTAL_EVENTS_IN_CACHE_BY_PRODUCER = "$METRIC_NAMESPACE.db.producer.total"
const val KAFKA_TOTAL_EVENTS_ON_TOPIC = "$METRIC_NAMESPACE.topic.aggregated.total"
const val KAFKA_TOTAL_EVENTS_ON_TOPIC_BY_PRODUCER = "$METRIC_NAMESPACE.topic.producer.total"
const val KAFKA_UNIQUE_EVENTS_ON_TOPIC = "$METRIC_NAMESPACE.topic.unique"
const val KAFKA_DUPLICATE_EVENTS_ON_TOPIC = "$METRIC_NAMESPACE.topic.duplicates"
