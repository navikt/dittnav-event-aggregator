package no.nav.personbruker.dittnav.eventaggregator.metrics

import no.nav.personbruker.dittnav.common.metrics.MetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.config.EventType

class RapidMetricsProbe(private val metricsReporter: MetricsReporter) {

    suspend fun countProcessed(eventType: EventType, producerApp: String) {
        metricsReporter.registerDataPoint(KAFKA_RAPID_EVENTS_PROCESSED, counterField(1), createTagMap(eventType.name, producerApp))
    }

    suspend fun countVarselInaktivertProduced() {
        metricsReporter.registerDataPoint(KAFKA_RAPID_VARSEL_INAKTIVERT_PRODUCED, counterField(1), emptyMap())

    }

    private fun counterField(events: Int): Map<String, Int> = listOf("counter" to events).toMap()

    private fun createTagMap(eventType: String, producer: String): Map<String, String> =
        listOf("eventType" to eventType, "producer" to producer).toMap()
}
