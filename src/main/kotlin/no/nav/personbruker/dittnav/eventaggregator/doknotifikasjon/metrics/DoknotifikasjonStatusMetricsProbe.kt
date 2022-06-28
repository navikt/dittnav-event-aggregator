package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.metrics

import no.nav.personbruker.dittnav.common.metrics.MetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.metrics.*

class DoknotifikasjonStatusMetricsProbe(private val metricsReporter: MetricsReporter) {
    suspend fun runWithMetrics(block: suspend DoknotifikasjonStatusMetricsSession.() -> Unit) {
        val session = DoknotifikasjonStatusMetricsSession()
        block.invoke(session)
        val processingTime = session.timeElapsedSinceSessionStartNanos()

        if (session.getTotalEventsProcessed() > 0) {
            handleStatusesProcessed(session)
            handleStatusesUpdated(session)
            handleStatusesUnchanged(session)
            handleStatusesUnmatched(session)
            handleBatchInfo(session, processingTime)
        }
    }

    private suspend fun handleStatusesProcessed(session: DoknotifikasjonStatusMetricsSession) {
        session.getTotalEventsByProducer().forEach { (bestiller, count) ->

            reportEvents(count, bestiller, KAFKA_DOKNOT_STATUS_TOTAL_PROCESSED)
        }
    }

    private suspend fun handleStatusesUpdated(session: DoknotifikasjonStatusMetricsSession) {
        reportForEachCount(KAFKA_DOKNOT_STATUS_UPDATED, session.getCountOfStatuesSuccessfullyUpdated())
    }

    private suspend fun handleStatusesUnchanged(session: DoknotifikasjonStatusMetricsSession) {
        reportForEachCount(KAFKA_DOKNOT_STATUS_UNCHANGED, session.getCountOfStatuesWithNoChange())
    }

    private suspend fun handleStatusesUnmatched(session: DoknotifikasjonStatusMetricsSession) {
        reportForEachCount(KAFKA_DOKNOT_STATUS_IGNORED, session.getCountOfStatuesWithNoMatch())
    }

    private suspend fun reportForEachCount(metricName: String, countList: List<TagPermutationWithCount>) {
        countList.forEach { countByTags ->
            reportEvents(
                count = countByTags.count,
                eventType = countByTags.eventType ,
                status = countByTags.status,
                producerName = countByTags.producer,
                metricName = metricName
            )
        }
    }

    private suspend fun handleBatchInfo(session: DoknotifikasjonStatusMetricsSession, processingTime: Long) {
        val fieldMap = listOf(
            "totalEvents" to session.getTotalEventsProcessed(),
            "processingTime" to processingTime
        ).toMap()

        metricsReporter.registerDataPoint(KAFKA_DOKNOT_STATUS_BATCH, fieldMap, emptyMap())
    }

    private suspend fun reportEvents(count: Int, producerName: String, metricName: String) {
        metricsReporter.registerDataPoint(metricName, counterField(count), mapOf("producer" to producerName))
    }

    private suspend fun reportEvents(count: Int, eventType: String, status: String, producerName: String, metricName: String) {
        metricsReporter.registerDataPoint(metricName, counterField(count), createTagMap(eventType, status, producerName))
    }

    private fun counterField(events: Int): Map<String, Int> = listOf("counter" to events).toMap()

    private fun createTagMap(eventType: String, status: String, producer: String): Map<String, String> =
        listOf("eventType" to eventType, "status" to status, "producer" to producer).toMap()
}
