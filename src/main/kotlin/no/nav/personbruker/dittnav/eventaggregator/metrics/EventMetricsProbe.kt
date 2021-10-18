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
        session.getUniqueProducers().forEach { producer ->
            val numberSeen = session.getEventsSeen(producer)
            val eventTypeName = session.eventType.toString()

            reportEvents(numberSeen, eventTypeName, producer.appnavn, KAFKA_EVENTS_SEEN, producer.namespace)
            PrometheusMetricsCollector.registerEventsSeen(numberSeen, eventTypeName, producer.appnavn)
        }
    }

    private suspend fun handleEventsProcessed(session: EventMetricsSession) {
        session.getUniqueProducers().forEach { producer ->
            val numberProcessed = session.getEventsProcessed(producer)
            val eventTypeName = session.eventType.toString()

            if (numberProcessed > 0) {
                reportEvents(numberProcessed, eventTypeName, producer.appnavn, KAFKA_EVENTS_PROCESSED, producer.namespace)
                PrometheusMetricsCollector.registerEventsProcessed(numberProcessed, eventTypeName, producer.appnavn)
            }
        }
    }

    private suspend fun handleEventsFailed(session: EventMetricsSession) {
        session.getUniqueProducers().forEach { producer ->
            val numberFailed = session.getEventsFailed(producer)
            val eventTypeName = session.eventType.toString()

            if (numberFailed > 0) {
                reportEvents(numberFailed, eventTypeName, producer.appnavn, KAFKA_EVENTS_FAILED, producer.namespace)
                PrometheusMetricsCollector.registerEventsFailed(numberFailed, eventTypeName, producer.appnavn)
            }
        }
    }

    private suspend fun handleDuplicateEventKeys(session: EventMetricsSession) {
        session.getUniqueProducers().forEach { producer ->
            val numberDuplicateKeyEvents = session.getDuplicateKeyEvents(producer)
            val eventTypeName = session.eventType.toString()

            if (numberDuplicateKeyEvents > 0) {
                reportEvents(numberDuplicateKeyEvents, eventTypeName, producer.appnavn, KAFKA_EVENTS_DUPLICATE_KEY, producer.namespace)
                PrometheusMetricsCollector.registerEventsDuplicateKey(numberDuplicateKeyEvents, eventTypeName, producer.appnavn)
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
