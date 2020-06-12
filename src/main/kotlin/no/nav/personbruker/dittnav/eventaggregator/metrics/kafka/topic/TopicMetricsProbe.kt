package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.topic

import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.metrics.MetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.metrics.ProducerNameScrubber
import no.nav.personbruker.dittnav.eventaggregator.metrics.PrometheusMetricsCollector
import no.nav.personbruker.dittnav.eventaggregator.metrics.influx.KAFKA_DUPLICATE_EVENTS_ON_TOPIC
import no.nav.personbruker.dittnav.eventaggregator.metrics.influx.KAFKA_TOTAL_EVENTS_ON_TOPIC
import no.nav.personbruker.dittnav.eventaggregator.metrics.influx.KAFKA_UNIQUE_EVENTS_ON_TOPIC
import org.slf4j.LoggerFactory

class TopicMetricsProbe(private val metricsReporter: MetricsReporter,
                        private val nameScrubber: ProducerNameScrubber) {

    private val log = LoggerFactory.getLogger(TopicMetricsProbe::class.java)

    suspend fun runWithMetrics(eventType: EventType, block: suspend TopicMetricsSession.() -> Unit) {
        val session = TopicMetricsSession(eventType)
        block.invoke(session)

        if (session.getNumberOfUniqueEvents() > 0) {
            handleUniqueEvents(session)
            handleDuplicatedEvents(session)
            handleTotalNumberOfEvents(session)
        }
    }

    private suspend fun handleUniqueEvents(session: TopicMetricsSession) {
        session.getProducersWithUniqueEvents().forEach { producerName ->
            val uniqueEvents = session.getNumberOfUniqueEvents()
            val eventTypeName = session.eventType.toString()
            val printableAlias = nameScrubber.getPublicAlias(producerName)

            reportEvents(uniqueEvents, eventTypeName, printableAlias, KAFKA_UNIQUE_EVENTS_ON_TOPIC)
            PrometheusMetricsCollector.registerUniqueEvents(uniqueEvents, session.eventType, printableAlias)
        }
    }

    private suspend fun handleDuplicatedEvents(session: TopicMetricsSession) {
        session.getProducersWithDuplicatedEvents().forEach { producerName ->
            val duplicates = session.getDuplicates()
            val eventTypeName = session.eventType.toString()
            val printableAlias = nameScrubber.getPublicAlias(producerName)

            reportEvents(duplicates, eventTypeName, printableAlias, KAFKA_DUPLICATE_EVENTS_ON_TOPIC)
            PrometheusMetricsCollector.registerDuplicatedEventsOnTopic(duplicates, session.eventType, printableAlias)
        }
    }

    private suspend fun handleTotalNumberOfEvents(session: TopicMetricsSession) {
        session.getProducersWithEvents().forEach { producerName ->
            val total = session.getTotalNumber()
            val eventTypeName = session.eventType.toString()
            val printableAlias = nameScrubber.getPublicAlias(producerName)

            reportEvents(total, eventTypeName, printableAlias, KAFKA_TOTAL_EVENTS_ON_TOPIC)
            PrometheusMetricsCollector.registerTotalNumberOfEvents(total, session.eventType, printableAlias)
        }
    }

    private suspend fun reportEvents(count: Int, eventType: String, producerAlias: String, metricName: String) {
        metricsReporter.registerDataPoint(metricName, counterField(count), createTagMap(eventType, producerAlias))
    }

    private fun counterField(events: Int): Map<String, Int> = listOf("counter" to events).toMap()

    private fun createTagMap(eventType: String, producer: String): Map<String, String> =
            listOf("eventType" to eventType, "producer" to producer).toMap()
}
