package no.nav.personbruker.dittnav.eventaggregator.metrics.db

import no.nav.personbruker.dittnav.common.metrics.MetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.metrics.DB_EVENTS_CACHED
import no.nav.personbruker.dittnav.eventaggregator.metrics.PrometheusMetricsCollector

class DBMetricsProbe(private val metricsReporter: MetricsReporter) {

    suspend fun runWithMetrics(eventType: EventType, block: suspend DBMetricsSession.() -> Unit) {
        val session = DBMetricsSession(eventType)
        block.invoke(session)
        if(session.getNumberOfCachedEvents() > 0) {
            handleCachedEvents(session)
        }
    }

    suspend fun handleCachedEvents(session: DBMetricsSession) {
        session.getUniqueProducers().forEach { producer ->
            val numberOfCachedEvents = session.getNumberOfCachedEventsForProducer(producer)
            val eventType = session.eventType
            metricsReporter.registerDataPoint(
                    DB_EVENTS_CACHED,
                    listOf("counter" to numberOfCachedEvents).toMap(),
                    listOf("eventType" to eventType.toString(),
                            "producer" to producer).toMap()
            )
            PrometheusMetricsCollector.registerEventsCached(numberOfCachedEvents, eventType, producer)
        }
    }
}
