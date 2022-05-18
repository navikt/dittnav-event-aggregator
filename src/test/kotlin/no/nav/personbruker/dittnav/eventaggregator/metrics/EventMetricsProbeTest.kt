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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class EventMetricsProbeTest {

    private val metricsReporter = mockk<MetricsReporter>()
    private val prometheusCollector = mockkObject(PrometheusMetricsCollector)

    @BeforeEach
    fun cleanup() {
        clearAllMocks()
    }

    @Test
    fun shouldUseProducerNameForEventProcessed() {
        val producer = Produsent("dummyAppnavn", "dummyNamespace")

        val metricsProbe = EventMetricsProbe(metricsReporter)

        val producerNameForPrometheus = slot<String>()
        val capturedTags = slot<Map<String, String>>()

        coEvery { metricsReporter.registerDataPoint(not(KAFKA_EVENTS_BATCH), any(), capture(capturedTags)) } returns Unit
        coEvery { metricsReporter.registerDataPoint(KAFKA_EVENTS_BATCH, any(), any()) } returns Unit
        every { PrometheusMetricsCollector.registerEventsSeen(any(), any(), capture(producerNameForPrometheus)) } returns Unit

        runBlocking {
            metricsProbe.runWithMetrics(EventType.BESKJED_INTERN) {
                countSuccessfulEventForProducer(producer)
            }
        }

        coVerify(exactly = 2) { metricsReporter.registerDataPoint(not(KAFKA_EVENTS_BATCH), any(), any()) }
        verify(exactly = 1) { PrometheusMetricsCollector.registerEventsSeen(any(), any(), any()) }
        verify(exactly = 1) { PrometheusMetricsCollector.registerEventsProcessed(any(), any(), any()) }

        producerNameForPrometheus.captured shouldBe producer.appnavn
        capturedTags.captured["producer"] shouldBe producer.appnavn
        capturedTags.captured["producerNamespace"] shouldBe producer.namespace
    }

    @Test
    fun shouldUseProducerNameForEventFailed() {
        val producer = Produsent("dummyAppnavn", "dummyNamespace")
        val metricsProbe = EventMetricsProbe(metricsReporter)

        val capturedTags = slot<Map<String, String>>()
        val producerNameForPrometheus = slot<String>()

        coEvery { metricsReporter.registerDataPoint(not(KAFKA_EVENTS_BATCH), any(), capture(capturedTags)) } returns Unit
        coEvery { metricsReporter.registerDataPoint(KAFKA_EVENTS_BATCH, any(), any()) } returns Unit
        every { PrometheusMetricsCollector.registerEventsFailed(any(), any(), capture(producerNameForPrometheus)) } returns Unit

        runBlocking {
            metricsProbe.runWithMetrics(EventType.BESKJED_INTERN) {
                countFailedEventForProducer(producer)
            }
        }

        coVerify(exactly = 2) { metricsReporter.registerDataPoint(not(KAFKA_EVENTS_BATCH), any(), any()) }
        verify(exactly = 1) { PrometheusMetricsCollector.registerEventsSeen(any(), any(), any()) }
        verify(exactly = 1) { PrometheusMetricsCollector.registerEventsFailed(any(), any(), any()) }

        producerNameForPrometheus.captured shouldBe producer.appnavn
        capturedTags.captured["producer"] shouldBe producer.appnavn
        capturedTags.captured["producerNamespace"] shouldBe producer.namespace
    }

    @Test
    fun shouldReportCorrectNumberOfEvents() {
        val metricsProbe = EventMetricsProbe(metricsReporter)

        val capturedFieldsForSeen = slot<Map<String, Any>>()
        val capturedFieldsForProcessed = slot<Map<String, Any>>()
        val capturedFieldsForFailed = slot<Map<String, Any>>()

        coEvery { metricsReporter.registerDataPoint(KAFKA_EVENTS_SEEN, capture(capturedFieldsForSeen), any()) } returns Unit
        coEvery { metricsReporter.registerDataPoint(KAFKA_EVENTS_PROCESSED, capture(capturedFieldsForProcessed), any()) } returns Unit
        coEvery { metricsReporter.registerDataPoint(KAFKA_EVENTS_FAILED, capture(capturedFieldsForFailed), any()) } returns Unit
        coEvery { metricsReporter.registerDataPoint(KAFKA_EVENTS_BATCH, any(), any()) } returns Unit

        runBlocking {
            metricsProbe.runWithMetrics(EventType.BESKJED_INTERN) {
                countSuccessfulEventForProducer(Produsent("dummyAppnavn", "dummyNamespace"))
                countSuccessfulEventForProducer(Produsent("dummyAppnavn", "dummyNamespace"))
                countFailedEventForProducer(Produsent("dummyAppnavn", "dummyNamespace"))
            }
        }

        coVerify(exactly = 4) { metricsReporter.registerDataPoint(any(), any(), any()) }
        verify(exactly = 1) { PrometheusMetricsCollector.registerEventsSeen(3, any(), any()) }
        verify(exactly = 1) { PrometheusMetricsCollector.registerEventsProcessed(2, any(), any()) }
        verify(exactly = 1) { PrometheusMetricsCollector.registerEventsFailed(1, any(), any()) }

        capturedFieldsForSeen.captured["counter"] shouldBe 3
        capturedFieldsForProcessed.captured["counter"] shouldBe 2
        capturedFieldsForFailed.captured["counter"] shouldBe 1
    }
}
