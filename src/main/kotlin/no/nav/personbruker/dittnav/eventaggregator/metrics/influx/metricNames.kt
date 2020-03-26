package no.nav.personbruker.dittnav.eventaggregator.metrics.influx

private const val METRIC_NAMESPACE = "dittnav.kafka.events.v1"

const val EVENTS_SEEN = "$METRIC_NAMESPACE.seen"
const val EVENTS_FAILED = "$METRIC_NAMESPACE.failed"
const val EVENTS_PROCESSED = "$METRIC_NAMESPACE.processed"
const val EVENTS_BATCH = "$METRIC_NAMESPACE.batch"
