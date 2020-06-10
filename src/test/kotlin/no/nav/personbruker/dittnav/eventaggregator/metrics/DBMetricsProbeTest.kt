package no.nav.personbruker.dittnav.eventaggregator.metrics

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.DBMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.influx.DB_EVENTS_CACHED
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DBMetricsProbeTest {

    private val metricsReporter = mockk<MetricsReporter>()
    private val prometheusCollector = mockkObject(PrometheusMetricsCollector)
    private val producerNameResolver = mockk<ProducerNameResolver>()

    @BeforeEach
    fun cleanup() {
        clearAllMocks()
    }

    @Test
    fun `skal bruke alias for systembruker`() {
        val producerName = "sys-t-user"
        val producerAlias = "test-user"

        coEvery { producerNameResolver.getProducerNameAlias(producerName) } returns producerAlias
        val nameScrubber = ProducerNameScrubber(producerNameResolver)
        val metricsProbe = DBMetricsProbe(metricsReporter, nameScrubber)

        val producerNameForPrometheus = slot<String>()

        coEvery { metricsReporter.registerDataPoint(DB_EVENTS_CACHED, any(), any()) } returns Unit
        every { PrometheusMetricsCollector.registerEventsCached(any(), any(), capture(producerNameForPrometheus)) } returns Unit

        runBlocking {
            metricsProbe.runWithMetrics(EventType.DONE) {
                countCachedEventForProducer(producerName)
            }
        }

        coVerify(exactly = 1) { metricsReporter.registerDataPoint(DB_EVENTS_CACHED, any(), any()) }
        verify(exactly = 1) { PrometheusMetricsCollector.registerEventsCached(any(), any(), any()) }

        assertEquals(producerAlias, producerNameForPrometheus.captured)
    }

    @Test
    fun `skal telle riktig antall eventer av gitt type i cache`() {
        coEvery { producerNameResolver.getProducerNameAlias(any()) } returns "test-user"
        val nameScrubber = ProducerNameScrubber(producerNameResolver)
        val metricsProbe = DBMetricsProbe(metricsReporter, nameScrubber)
        val capturedFieldsForCachedEvents = slot<Map<String, Any>>()
        coEvery { metricsReporter.registerDataPoint(DB_EVENTS_CACHED, capture(capturedFieldsForCachedEvents), any()) } returns Unit

        runBlocking {
            metricsProbe.runWithMetrics(EventType.DONE) {
                countCachedEventForProducer("dummyProducer")
                countCachedEventForProducer("dummyProducer")
            }
        }

        coVerify(exactly = 1) { metricsReporter.registerDataPoint(
                DB_EVENTS_CACHED,
                listOf("counter" to 2).toMap(),
                listOf("eventType" to EventType.DONE.toString(), "producer" to "test-user").toMap()) }
        verify(exactly = 1) { PrometheusMetricsCollector.registerEventsCached(2, EventType.DONE, "test-user") }

        assertEquals(capturedFieldsForCachedEvents.captured["counter"] as Int, 2)
    }
}
