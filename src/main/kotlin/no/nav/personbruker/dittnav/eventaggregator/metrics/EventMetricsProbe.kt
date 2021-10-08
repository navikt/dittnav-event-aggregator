package no.nav.personbruker.dittnav.eventaggregator.metrics

import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.common.metrics.MetricsReporter

class EventMetricsProbe(private val metricsReporter: MetricsReporter) {

    suspend fun runWithMetrics(eventType: EventType, block: suspend EventMetricsSession.() -> Unit) {
        val session = EventMetricsSession(eventType)
        block.invoke(session)
        val processingTime = session.timeElapsedSinceSessionStartNanos()

        if (session.getEventsSeen() > 0) {
            handleEventsSeen(session)
            handleEventsProcessed(session)
            handleEventsFailed(session)
            handleDuplicateEventKeys(session)
            handleEventsBatch(session, processingTime)
        }
    }

    private suspend fun handleEventsSeen(session: EventMetricsSession) {
        session.getUniqueProducers().forEach { producerName ->
            val numberSeen = session.getEventsSeen(producerName)
            val eventTypeName = session.eventType.toString()
            val producerNamespace = session.getNamespace()

            reportEvents(numberSeen, eventTypeName, producerName, KAFKA_EVENTS_SEEN, producerNamespace)
            PrometheusMetricsCollector.registerEventsSeen(numberSeen, eventTypeName, producerName)
        }
    }

    private suspend fun handleEventsProcessed(session: EventMetricsSession) {
        session.getUniqueProducers().forEach { producerName ->
            val numberProcessed = session.getEventsProcessed(producerName)
            val eventTypeName = session.eventType.toString()
            val producerNamespace = session.getNamespace()

            if (numberProcessed > 0) {
                reportEvents(numberProcessed, eventTypeName, producerName, KAFKA_EVENTS_PROCESSED, producerNamespace)
                PrometheusMetricsCollector.registerEventsProcessed(numberProcessed, eventTypeName, producerName)
            }
        }
    }

    private suspend fun handleEventsFailed(session: EventMetricsSession) {
        session.getUniqueProducers().forEach { producerName ->
            val numberFailed = session.getEventsFailed(producerName)
            val eventTypeName = session.eventType.toString()
            val producerNamespace = session.getNamespace()

            if (numberFailed > 0) {
                reportEvents(numberFailed, eventTypeName, producerName, KAFKA_EVENTS_FAILED, producerNamespace)
                PrometheusMetricsCollector.registerEventsFailed(numberFailed, eventTypeName, producerName)
            }
        }
    }

    private suspend fun handleDuplicateEventKeys(session: EventMetricsSession) {
        session.getUniqueProducers().forEach { producerName ->
            val numberDuplicateKeyEvents = session.getDuplicateKeyEvents(producerName)
            val eventTypeName = session.eventType.toString()
            val producerNamespace = session.getNamespace()

            if (numberDuplicateKeyEvents > 0) {
                reportEvents(numberDuplicateKeyEvents, eventTypeName, producerName, KAFKA_EVENTS_DUPLICATE_KEY, producerNamespace)
                PrometheusMetricsCollector.registerEventsDuplicateKey(numberDuplicateKeyEvents, eventTypeName, producerName)
            }
        }
    }

    private suspend fun handleEventsBatch(session: EventMetricsSession, processingTime: Long) {
        val metricsOverHead = session.timeElapsedSinceSessionStartNanos() - processingTime
        val fieldMap = listOf(
                "seen" to session.getEventsSeen(),
                "processed" to session.getEventsProcessed(),
                "failed" to session.getEventsFailed(),
                "processingTime" to processingTime,
                "metricsOverheadTime" to metricsOverHead
        ).toMap()

        val tagMap = listOf("eventType" to session.eventType.toString()).toMap()

        metricsReporter.registerDataPoint(KAFKA_EVENTS_BATCH, fieldMap, tagMap)
    }

    private suspend fun reportEvents(count: Int, eventType: String, producerName: String, metricName: String, producerNamespace: String) {
        metricsReporter.registerDataPoint(metricName, counterField(count), createTagMap(eventType, producerName, producerNamespace))
    }

    private fun counterField(events: Int): Map<String, Int> = listOf("counter" to events).toMap()

    private fun createTagMap(eventType: String, producer: String, producerNamespace: String): Map<String, String> =
            listOf("eventType" to eventType, "producer" to producer, "producerNamespace" to producerNamespace).toMap()
}
