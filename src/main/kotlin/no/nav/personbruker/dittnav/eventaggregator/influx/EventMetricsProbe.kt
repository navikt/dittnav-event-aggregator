package no.nav.personbruker.dittnav.eventaggregator.influx

import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.PrometheusMetricsCollector
import org.slf4j.LoggerFactory

class EventMetricsProbe (val metricsReporter: MetricsReporter) {

    val log = LoggerFactory.getLogger(EventMetricsProbe::class.java)

    suspend fun reportEventSeen(eventType: EventType, producer: String) {
        reportEvent(eventType.toString(), producer, EVENTS_SEEN)
        PrometheusMetricsCollector.registerMessageSeen(eventType.toString(), producer)
    }

    suspend fun reportEventProcessed(eventType: EventType, producer: String) {
        reportEvent(eventType.toString(), producer, EVENTS_PROCESSED)
    }

    suspend fun reportEventFailed(eventType: EventType, producer: String) {
        reportEvent(eventType.toString(), producer, EVENTS_FAILED)
    }

    private suspend fun reportEvent(eventType: String, producer: String, metricName: String) {
        metricsReporter.registerDataPoint(metricName, counterIncrement, createTagMap(eventType, producer))
    }

    private val counterIncrement: Map<String, Int> = listOf("counter" to 1).toMap()

    private fun createTagMap(eventType: String, producer: String): Map<String, String> =
            listOf("eventType" to eventType, "producer" to producer).toMap()
}