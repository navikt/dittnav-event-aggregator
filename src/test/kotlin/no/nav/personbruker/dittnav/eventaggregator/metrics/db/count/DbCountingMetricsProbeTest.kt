package no.nav.personbruker.dittnav.eventaggregator.metrics.db.count

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.metrics.MetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.metrics.ProducerNameResolver
import no.nav.personbruker.dittnav.eventaggregator.metrics.ProducerNameScrubber
import no.nav.personbruker.dittnav.eventaggregator.metrics.PrometheusMetricsCollector
import no.nav.personbruker.dittnav.eventaggregator.metrics.influx.DB_TOTAL_EVENTS_IN_CACHE
import no.nav.personbruker.dittnav.eventaggregator.metrics.influx.DB_TOTAL_EVENTS_IN_CACHE_BY_PRODUCER
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DbCountingMetricsProbeTest {

    private val metricsReporter = mockk<MetricsReporter>()
    private val prometheusCollector = mockkObject(PrometheusMetricsCollector)
    private val producerNameResolver = mockk<ProducerNameResolver>()

    @BeforeEach
    fun cleanup() {
        clearAllMocks()
    }

    @Test
    fun `Should report correct number of events`() {
        val dummyCountResultFromDb = mutableMapOf<String, Int>().apply {
            put("produsent1", 1)
            put("produsent2", 2)
        }

        coEvery { producerNameResolver.getProducerNameAlias(any()) } returns "test-user"
        val nameScrubber = ProducerNameScrubber(producerNameResolver)
        val topicMetricsProbe = DbCountingMetricsProbe(metricsReporter, nameScrubber)

        val capturedTotalEventsInCacheByProducer = slot<Map<String, Any>>()

        coEvery { metricsReporter.registerDataPoint(DB_TOTAL_EVENTS_IN_CACHE, any(), any()) } returns Unit
        coEvery { metricsReporter.registerDataPoint(DB_TOTAL_EVENTS_IN_CACHE_BY_PRODUCER, capture(capturedTotalEventsInCacheByProducer), any()) } returns Unit

        runBlocking {
            topicMetricsProbe.runWithMetrics(EventType.BESKJED) {
                addEventsByProducer(dummyCountResultFromDb)
            }
        }

        coVerify(exactly = 3) { metricsReporter.registerDataPoint(any(), any(), any()) }
        verify(exactly = 1) { PrometheusMetricsCollector.registerTotalNumberOfEventsInCache(3, any()) }
        verify(exactly = 1) { PrometheusMetricsCollector.registerTotalNumberOfEventsInCacheByProducer(1, any(), any()) }
        verify(exactly = 1) { PrometheusMetricsCollector.registerTotalNumberOfEventsInCacheByProducer(2, any(), any()) }

        kotlin.test.assertEquals(1, capturedTotalEventsInCacheByProducer.captured["counter"])
    }

    @Test
    fun `Should replace system name with alias`() {
        val producerName = "sys-t-user"
        val producerAlias = "test-user"

        coEvery { producerNameResolver.getProducerNameAlias(producerName) } returns producerAlias
        val nameScrubber = ProducerNameScrubber(producerNameResolver)
        val metricsProbe = DbCountingMetricsProbe(metricsReporter, nameScrubber)

        val producerNameForPrometheus = slot<String>()
        val capturedTagsForTotalByProducer = slot<Map<String, String>>()

        coEvery { metricsReporter.registerDataPoint(DB_TOTAL_EVENTS_IN_CACHE, any(), any()) } returns Unit
        coEvery { metricsReporter.registerDataPoint(DB_TOTAL_EVENTS_IN_CACHE_BY_PRODUCER, any(), capture(capturedTagsForTotalByProducer)) } returns Unit
        every { PrometheusMetricsCollector.registerTotalNumberOfEventsInCacheByProducer(any(), any(), capture(producerNameForPrometheus)) } returns Unit

        runBlocking {
            metricsProbe.runWithMetrics(EventType.BESKJED) {
                addEventsByProducer(mapOf(Pair(producerName, 2)))
            }
        }

        coVerify(exactly = 1) { metricsReporter.registerDataPoint(DB_TOTAL_EVENTS_IN_CACHE_BY_PRODUCER, any(), any()) }

        verify(exactly = 1) { PrometheusMetricsCollector.registerTotalNumberOfEventsInCacheByProducer(any(), any(), any()) }

        kotlin.test.assertEquals(producerAlias, producerNameForPrometheus.captured)
        kotlin.test.assertEquals(producerAlias, capturedTagsForTotalByProducer.captured["producer"])
    }

}
