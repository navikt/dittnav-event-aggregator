package no.nav.personbruker.dittnav.eventaggregator.archive

import no.nav.personbruker.dittnav.common.metrics.MetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.metrics.DB_EVENTS_ARCHIVED

class ArchiveMetricsProbe(private val metricsReporter: MetricsReporter) {
    suspend fun countEntitiesArchived(eventType: EventType, events: List<VarselArchiveDTO>) {
        events.groupBy { it.produsentApp }
            .forEach { (produsentApp, archived) ->
                reportArchived(archived.size, eventType, produsentApp)
            }
    }

    private suspend fun reportArchived(count: Int, eventType: EventType, producerApp: String) {
        metricsReporter.registerDataPoint(DB_EVENTS_ARCHIVED, counterField(count), createTagMap(eventType.name, producerApp))
    }

    private fun counterField(events: Int): Map<String, Int> = listOf("counter" to events).toMap()

    private fun createTagMap(eventType: String, producer: String): Map<String, String> =
        listOf("eventType" to eventType, "producer" to producer).toMap()
}
