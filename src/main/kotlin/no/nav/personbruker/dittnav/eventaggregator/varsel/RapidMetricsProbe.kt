package no.nav.personbruker.dittnav.eventaggregator.varsel

import no.nav.personbruker.dittnav.common.metrics.MetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.metrics.KAFKA_RAPID_EVENTS_PROCESSED

class RapidMetricsProbe(private val metricsReporter: MetricsReporter) {

    suspend fun countProcessed(eventType: EventType, producerApp: String) {
        metricsReporter.registerDataPoint(KAFKA_RAPID_EVENTS_PROCESSED, counterField(1), createTagMap(eventType.name, producerApp))
    }

    private fun counterField(events: Int): Map<String, Int> = listOf("counter" to events).toMap()

    private fun createTagMap(eventType: String, producer: String): Map<String, String> =
        listOf("eventType" to eventType, "producer" to producer).toMap()
}
