package no.nav.personbruker.dittnav.eventaggregator.metrics

import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.metrics.influx.*
import org.slf4j.LoggerFactory

class EventMetricsProbe (private val metricsReporter: MetricsReporter,
                         private val nameScrubber: ProducerNameScrubber) {

    val log = LoggerFactory.getLogger(EventMetricsProbe::class.java)

    suspend fun reportEventSeen(eventType: EventType, producer: String) {
        val printableAlias = nameScrubber.getPublicAlias(producer)
        reportEvent(eventType.toString(), printableAlias, EVENTS_SEEN)
        PrometheusMetricsCollector.registerMessageSeen(eventType.toString(), printableAlias)
    }

    suspend fun reportEventProcessed(eventType: EventType, producer: String) {
        val printableAlias = nameScrubber.getPublicAlias(producer)
        reportEvent(eventType.toString(), printableAlias, EVENTS_PROCESSED)
        PrometheusMetricsCollector.registerMessageProcessed(eventType.toString(), printableAlias)
    }

    suspend fun reportEventFailed(eventType: EventType, producer: String) {
        val printableAlias = nameScrubber.getPublicAlias(producer)
        reportEvent(eventType.toString(), printableAlias, EVENTS_FAILED)
        PrometheusMetricsCollector.registerMessageFailed(eventType.toString(), printableAlias)
    }

    private suspend fun reportEvent(eventType: String, producerAlias: String, metricName: String) {
        metricsReporter.registerDataPoint(metricName, counterIncrement, createTagMap(eventType, producerAlias))
    }

    private val counterIncrement: Map<String, Int> = listOf("counter" to 1).toMap()

    private fun createTagMap(eventType: String, producer: String): Map<String, String> =
            listOf("eventType" to eventType, "producer" to producer).toMap()
}