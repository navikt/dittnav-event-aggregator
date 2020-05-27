package no.nav.personbruker.dittnav.eventaggregator.metrics.db

import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.metrics.MetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.metrics.PrometheusMetricsCollector
import no.nav.personbruker.dittnav.eventaggregator.metrics.influx.EVENTS_CACHED

class DBMetricsProbe(private val metricsReporter: MetricsReporter) {

    suspend fun numberOfCachedEventsOfType(numberOfEvents: Int, eventType: EventType) {
        metricsReporter.registerDataPoint(
                EVENTS_CACHED,
                listOf("numberOfEvents" to numberOfEvents).toMap(),
                listOf("eventType" to eventType.eventType).toMap()
        )
        PrometheusMetricsCollector.registerEventsCached(numberOfEvents, eventType)
    }
}
