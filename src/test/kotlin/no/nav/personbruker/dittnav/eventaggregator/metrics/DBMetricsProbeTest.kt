package no.nav.personbruker.dittnav.eventaggregator.metrics

import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.common.metrics.MetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.DBMetricsProbe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DBMetricsProbeTest {

    private val metricsReporter = mockk<MetricsReporter>()
    private val prometheusCollector = mockkObject(PrometheusMetricsCollector)

    @BeforeEach
    fun cleanup() {
        clearAllMocks()
    }

    @Test
    fun `skal bruke appnavn som produsent navn`() {
        val producerName = "appnavn"

        val metricsProbe = DBMetricsProbe(metricsReporter)

        val producerNameForPrometheus = slot<String>()

        coEvery { metricsReporter.registerDataPoint(DB_EVENTS_CACHED, any(), any()) } returns Unit
        every { PrometheusMetricsCollector.registerEventsCached(any(), any(), capture(producerNameForPrometheus)) } returns Unit

        runBlocking {
            metricsProbe.runWithMetrics(EventType.DONE_INTERN) {
                countCachedEventForProducer(producerName)
            }
        }

        coVerify(exactly = 1) { metricsReporter.registerDataPoint(DB_EVENTS_CACHED, any(), any()) }
        verify(exactly = 1) { PrometheusMetricsCollector.registerEventsCached(any(), any(), any()) }

        producerNameForPrometheus.captured shouldBe producerName
    }

    @Test
    fun `skal telle riktig antall eventer av gitt type i cache`() {
        val metricsProbe = DBMetricsProbe(metricsReporter)
        val capturedFieldsForCachedEvents = slot<Map<String, Any>>()
        coEvery { metricsReporter.registerDataPoint(DB_EVENTS_CACHED, capture(capturedFieldsForCachedEvents), any()) } returns Unit

        runBlocking {
            metricsProbe.runWithMetrics(EventType.DONE_INTERN) {
                countCachedEventForProducer("dummyProducer")
                countCachedEventForProducer("dummyProducer")
            }
        }

        coVerify(exactly = 1) { metricsReporter.registerDataPoint(
                DB_EVENTS_CACHED,
                listOf("counter" to 2).toMap(),
                listOf("eventType" to EventType.DONE_INTERN.toString(), "producer" to "dummyProducer").toMap()) }
        verify(exactly = 1) { PrometheusMetricsCollector.registerEventsCached(2, EventType.DONE_INTERN, "dummyProducer") }

        capturedFieldsForCachedEvents.captured["counter"] shouldBe 2
    }
}
