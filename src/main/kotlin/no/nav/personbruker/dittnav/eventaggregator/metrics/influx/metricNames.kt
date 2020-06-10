package no.nav.personbruker.dittnav.eventaggregator.metrics.influx

private const val METRIC_NAMESPACE = "dittnav.kafka.events.v1"

const val KAFKA_EVENTS_SEEN = "$METRIC_NAMESPACE.seen"
const val KAFKA_EVENTS_FAILED = "$METRIC_NAMESPACE.failed"
const val KAFKA_EVENTS_PROCESSED = "$METRIC_NAMESPACE.processed"
const val KAFKA_EVENTS_DUPLICATE_KEY = "$METRIC_NAMESPACE.duplicateKey"
const val KAFKA_EVENTS_BATCH = "$METRIC_NAMESPACE.batch"
const val DB_EVENTS_CACHED = "$METRIC_NAMESPACE.cached"
