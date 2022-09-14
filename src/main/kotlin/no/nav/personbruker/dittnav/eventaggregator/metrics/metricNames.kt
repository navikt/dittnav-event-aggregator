package no.nav.personbruker.dittnav.eventaggregator.metrics

private const val METRIC_NAMESPACE = "dittnav.kafka.events.v1"

const val KAFKA_EVENTS_SEEN = "$METRIC_NAMESPACE.seen"
const val KAFKA_EVENTS_FAILED = "$METRIC_NAMESPACE.failed"
const val KAFKA_EVENTS_PROCESSED = "$METRIC_NAMESPACE.processed"
const val KAFKA_EVENTS_DUPLICATE_KEY = "$METRIC_NAMESPACE.duplicateKey"
const val KAFKA_EVENTS_BATCH = "$METRIC_NAMESPACE.batch"
const val DB_EVENTS_CACHED = "$METRIC_NAMESPACE.cached"

const val KAFKA_RAPID_EVENTS_PROCESSED = "$METRIC_NAMESPACE.rapid.processed"

const val KAFKA_DOKNOT_STATUS_TOTAL_PROCESSED = "$METRIC_NAMESPACE.doknot.status.total.processed"
const val KAFKA_DOKNOT_STATUS_UPDATED = "$METRIC_NAMESPACE.doknot.status.updated"
const val KAFKA_DOKNOT_STATUS_IGNORED = "$METRIC_NAMESPACE.doknot.status.ignored"
const val KAFKA_DOKNOT_STATUS_BATCH = "$METRIC_NAMESPACE.doknot.status.batch"

const val DB_EVENTS_ARCHIVED = "$METRIC_NAMESPACE.db.events.archived"
