package no.nav.personbruker.dittnav.eventaggregator.metrics

import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.metrics.influx.*

class EventMetricsProbe(private val metricsReporter: MetricsReporter,
                        private val nameScrubber: ProducerNameScrubber) {

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
            val printableAlias = nameScrubber.getPublicAlias(producerName)

            reportEvents(numberSeen, eventTypeName, printableAlias, EVENTS_SEEN)
            PrometheusMetricsCollector.registerEventsSeen(numberSeen, eventTypeName, printableAlias)
        }
    }

    private suspend fun handleEventsProcessed(session: EventMetricsSession) {
        session.getUniqueProducers().forEach { producerName ->
            val numberProcessed = session.getEventsProcessed(producerName)
            val eventTypeName = session.eventType.toString()
            val printableAlias = nameScrubber.getPublicAlias(producerName)

            if (numberProcessed > 0) {
                reportEvents(numberProcessed, eventTypeName, printableAlias, EVENTS_PROCESSED)
                PrometheusMetricsCollector.registerEventsProcessed(numberProcessed, eventTypeName, printableAlias)
            }
        }
    }

    private suspend fun handleEventsFailed(session: EventMetricsSession) {
        session.getUniqueProducers().forEach { producerName ->
            val numberFailed = session.getEventsFailed(producerName)
            val eventTypeName = session.eventType.toString()
            val printableAlias = nameScrubber.getPublicAlias(producerName)

            if (numberFailed > 0) {
                reportEvents(numberFailed, eventTypeName, printableAlias, EVENTS_FAILED)
                PrometheusMetricsCollector.registerEventsFailed(numberFailed, eventTypeName, printableAlias)
            }
        }
    }

    private suspend fun handleDuplicateEventKeys(session: EventMetricsSession) {
        session.getUniqueProducers().forEach { producerName ->
            val numberDuplicateKeyEvents = session.getDuplicateKeyEvents(producerName)
            val eventTypeName = session.eventType.toString()
            val printableAlias = nameScrubber.getPublicAlias(producerName)

            if (numberDuplicateKeyEvents > 0) {
                reportEvents(numberDuplicateKeyEvents, eventTypeName, printableAlias, EVENTS_DUPLICATE_KEY)
                PrometheusMetricsCollector.registerEventsDuplicateKey(numberDuplicateKeyEvents, eventTypeName, printableAlias)
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

        metricsReporter.registerDataPoint(EVENTS_BATCH, fieldMap, tagMap)
    }

    private suspend fun reportEvents(count: Int, eventType: String, producerAlias: String, metricName: String) {
        metricsReporter.registerDataPoint(metricName, counterField(count), createTagMap(eventType, producerAlias))
    }

    private fun counterField(events: Int): Map<String, Int> = listOf("counter" to events).toMap()

    private fun createTagMap(eventType: String, producer: String): Map<String, String> =
            listOf("eventType" to eventType, "producer" to producer).toMap()
}