package no.nav.personbruker.dittnav.eventaggregator.metrics.db.count

import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.metrics.MetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.metrics.ProducerNameScrubber
import no.nav.personbruker.dittnav.eventaggregator.metrics.PrometheusMetricsCollector
import no.nav.personbruker.dittnav.eventaggregator.metrics.influx.DB_TOTAL_EVENTS_IN_CACHE_BY_PRODUCER
import org.slf4j.LoggerFactory

class DbCountingMetricsProbe(private val metricsReporter: MetricsReporter,
                             private val nameScrubber: ProducerNameScrubber) {

    private val log = LoggerFactory.getLogger(DbCountingMetricsProbe::class.java)

    suspend fun runWithMetrics(eventType: EventType, block: suspend DbCountingMetricsSession.() -> Unit) {
        val session = DbCountingMetricsSession(eventType)
        block.invoke(session)

        if (session.getProducers().isNotEmpty()) {
            handleTotalEventsByProducer(session)
        }
    }

    private suspend fun handleTotalEventsByProducer(session: DbCountingMetricsSession) {
        session.getProducers().forEach { producerName ->
            val numberOfEvents = session.getNumberOfEventsFor(producerName)
            val eventTypeName = session.eventType.toString()
            val printableAlias = nameScrubber.getPublicAlias(producerName)

            reportEvents(numberOfEvents, eventTypeName, printableAlias, DB_TOTAL_EVENTS_IN_CACHE_BY_PRODUCER)
            PrometheusMetricsCollector.registerTotalNumberOfEventsInCacheByProducer(numberOfEvents, session.eventType, printableAlias)
        }
    }

    private suspend fun reportEvents(count: Int, eventType: String, producerAlias: String, metricName: String) {
        metricsReporter.registerDataPoint(metricName, counterField(count), createTagMap(eventType, producerAlias))
    }

    private fun counterField(events: Int): Map<String, Int> = listOf("counter" to events).toMap()

    private fun createTagMap(eventType: String, producer: String): Map<String, String> =
            listOf("eventType" to eventType, "producer" to producer).toMap()

}
