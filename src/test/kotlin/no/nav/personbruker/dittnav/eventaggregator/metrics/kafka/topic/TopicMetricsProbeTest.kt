package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.topic

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.metrics.MetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.metrics.ProducerNameResolver
import no.nav.personbruker.dittnav.eventaggregator.metrics.ProducerNameScrubber
import no.nav.personbruker.dittnav.eventaggregator.metrics.PrometheusMetricsCollector
import no.nav.personbruker.dittnav.eventaggregator.metrics.influx.KAFKA_DUPLICATE_EVENTS_ON_TOPIC
import no.nav.personbruker.dittnav.eventaggregator.metrics.influx.KAFKA_TOTAL_EVENTS_ON_TOPIC_BY_PRODUCER
import no.nav.personbruker.dittnav.eventaggregator.metrics.influx.KAFKA_UNIQUE_EVENTS_ON_TOPIC
import no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.UniqueKafkaEventIdentifier
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class TopicMetricsProbeTest {

    private val metricsReporter = mockk<MetricsReporter>()
    private val prometheusCollector = mockkObject(PrometheusMetricsCollector)
    private val producerNameResolver = mockk<ProducerNameResolver>()

    @BeforeEach
    fun cleanup() {
        clearAllMocks()
    }

    @Test
    fun `Should report correct number of events`() {
        coEvery { producerNameResolver.getProducerNameAlias(any()) } returns "test-user"
        val nameScrubber = ProducerNameScrubber(producerNameResolver)
        val topicMetricsProbe = TopicMetricsProbe(metricsReporter, nameScrubber)

        val capturedFieldsForUnique = slot<Map<String, Any>>()
        val capturedFieldsForTotalEvents = slot<Map<String, Any>>()
        val capturedFieldsForDuplicated = slot<Map<String, Any>>()

        coEvery { metricsReporter.registerDataPoint(KAFKA_UNIQUE_EVENTS_ON_TOPIC, capture(capturedFieldsForUnique), any()) } returns Unit
        coEvery { metricsReporter.registerDataPoint(KAFKA_TOTAL_EVENTS_ON_TOPIC_BY_PRODUCER, capture(capturedFieldsForTotalEvents), any()) } returns Unit
        coEvery { metricsReporter.registerDataPoint(KAFKA_DUPLICATE_EVENTS_ON_TOPIC, capture(capturedFieldsForDuplicated), any()) } returns Unit

        runBlocking {
            topicMetricsProbe.runWithMetrics(EventType.BESKJED) {
                countEvent(UniqueKafkaEventIdentifier("1", "producer", "123"))
                countEvent(UniqueKafkaEventIdentifier("2", "producer", "123"))
                countEvent(UniqueKafkaEventIdentifier("3", "producer", "123"))
                countEvent(UniqueKafkaEventIdentifier("3", "producer", "123"))
            }
        }

        coVerify(exactly = 3) { metricsReporter.registerDataPoint(any(), any(), any()) }
        verify(exactly = 1) { PrometheusMetricsCollector.registerUniqueEvents(3, any(), any()) }
        verify(exactly = 1) { PrometheusMetricsCollector.registerTotalNumberOfEventsByProducer(4, any(), any()) }
        verify(exactly = 1) { PrometheusMetricsCollector.registerDuplicatedEventsOnTopic(1, any(), any()) }

        assertEquals(3, capturedFieldsForUnique.captured["counter"])
        assertEquals(4, capturedFieldsForTotalEvents.captured["counter"])
        assertEquals(1, capturedFieldsForDuplicated.captured["counter"])
    }

    @Test
    fun `Should replace system name with alias`() {
        val producerName = "sys-t-user"
        val producerAlias = "test-user"

        coEvery { producerNameResolver.getProducerNameAlias(producerName) } returns producerAlias
        val nameScrubber = ProducerNameScrubber(producerNameResolver)
        val metricsProbe = TopicMetricsProbe(metricsReporter, nameScrubber)

        val producerNameForPrometheus = slot<String>()
        val capturedTagsForUnique = slot<Map<String, String>>()
        val capturedTagsForTotal = slot<Map<String, String>>()
        val capturedTagsForDuplicates = slot<Map<String, String>>()

        coEvery { metricsReporter.registerDataPoint(KAFKA_TOTAL_EVENTS_ON_TOPIC_BY_PRODUCER, any(), capture(capturedTagsForTotal)) } returns Unit
        coEvery { metricsReporter.registerDataPoint(KAFKA_UNIQUE_EVENTS_ON_TOPIC, any(), capture(capturedTagsForUnique)) } returns Unit
        coEvery { metricsReporter.registerDataPoint(KAFKA_DUPLICATE_EVENTS_ON_TOPIC, any(), capture(capturedTagsForDuplicates)) } returns Unit
        every { PrometheusMetricsCollector.registerUniqueEvents(any(), any(), capture(producerNameForPrometheus)) } returns Unit

        runBlocking {
            metricsProbe.runWithMetrics(EventType.BESKJED) {
                countEvent(UniqueKafkaEventIdentifier("1", producerName, "123"))
                countEvent(UniqueKafkaEventIdentifier("1", producerName, "123"))
            }
        }

        coVerify(exactly = 1) { metricsReporter.registerDataPoint(KAFKA_TOTAL_EVENTS_ON_TOPIC_BY_PRODUCER, any(), any()) }
        coVerify(exactly = 1) { metricsReporter.registerDataPoint(KAFKA_UNIQUE_EVENTS_ON_TOPIC, any(), any()) }
        coVerify(exactly = 1) { metricsReporter.registerDataPoint(KAFKA_DUPLICATE_EVENTS_ON_TOPIC, any(), any()) }

        verify(exactly = 1) { PrometheusMetricsCollector.registerUniqueEvents(any(), any(), any()) }
        verify(exactly = 1) { PrometheusMetricsCollector.registerDuplicatedEventsOnTopic(any(), any(), any()) }
        verify(exactly = 1) { PrometheusMetricsCollector.registerTotalNumberOfEventsByProducer(any(), any(), any()) }

        assertEquals(producerAlias, producerNameForPrometheus.captured)
        assertEquals(producerAlias, capturedTagsForUnique.captured["producer"])
        assertEquals(producerAlias, capturedTagsForTotal.captured["producer"])
        assertEquals(producerAlias, capturedTagsForDuplicates.captured["producer"])
    }

}
