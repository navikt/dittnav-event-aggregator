package no.nav.personbruker.dittnav.eventaggregator.metrics

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class EventMetricsProbeTest {
    val metricsReporter = mockk<MetricsReporter>()
    val prometheusCollector = mockkObject(PrometheusMetricsCollector)

    @BeforeEach
    fun cleanup() {
        clearAllMocks()
    }

    @Test
    fun shouldReplaceSystemNameWithAliasForEventSeen() {
        val producerName = "sys-t-user"
        val producerAlias = "test-user"
        val aliasEnvVariable = "$producerName:$producerAlias"
        val nameScrubber = ProducerNameScrubber(aliasEnvVariable)
        val metricsProbe = EventMetricsProbe(metricsReporter, nameScrubber)

        val producerNameForPrometheus = slot<String>()
        val capturedTags = slot<Map<String, String>>()

        coEvery { metricsReporter.registerDataPoint(any(), any(), capture(capturedTags)) } returns Unit
        every { PrometheusMetricsCollector.registerMessageSeen(any(), capture(producerNameForPrometheus)) } returns Unit

        runBlocking {
            metricsProbe.reportEventSeen(EventType.BESKJED, producerName)
        }

        coVerify(exactly = 1) { metricsReporter.registerDataPoint(any(), any(), any()) }
        verify(exactly = 1) { PrometheusMetricsCollector.registerMessageSeen(any(), any()) }

        assertEquals(producerAlias, producerNameForPrometheus.captured)
        assertEquals(producerAlias, capturedTags.captured["producer"])
    }

    @Test
    fun shouldReplaceSystemNameWithAliasForEventProcessed() {
        val producerName = "sys-t-user"
        val producerAlias = "test-user"
        val aliasEnvVariable = "$producerName:$producerAlias"
        val nameScrubber = ProducerNameScrubber(aliasEnvVariable)
        val metricsProbe = EventMetricsProbe(metricsReporter, nameScrubber)

        val capturedTags = slot<Map<String, String>>()
        val producerNameForPrometheus = slot<String>()

        coEvery { metricsReporter.registerDataPoint(any(), any(), capture(capturedTags)) } returns Unit
        every { PrometheusMetricsCollector.registerMessageProcessed(any(), capture(producerNameForPrometheus)) } returns Unit

        runBlocking {
            metricsProbe.reportEventProcessed(EventType.BESKJED, producerName)
        }

        coVerify(exactly = 1) { metricsReporter.registerDataPoint(any(), any(), any()) }
        verify(exactly = 1) { PrometheusMetricsCollector.registerMessageProcessed(any(), any()) }

        assertEquals(producerAlias, producerNameForPrometheus.captured)
        assertEquals(producerAlias, capturedTags.captured["producer"])
    }

    @Test
    fun shouldReplaceSystemNameWithAliasForEventFailed() {
        val producerName = "sys-t-user"
        val producerAlias = "test-user"
        val aliasEnvVariable = "$producerName:$producerAlias"
        val nameScrubber = ProducerNameScrubber(aliasEnvVariable)
        val metricsProbe = EventMetricsProbe(metricsReporter, nameScrubber)

        val capturedTags = slot<Map<String, String>>()
        val producerNameForPrometheus = slot<String>()

        coEvery { metricsReporter.registerDataPoint(any(), any(), capture(capturedTags)) } returns Unit
        every { PrometheusMetricsCollector.registerMessageFailed(any(), capture(producerNameForPrometheus)) } returns Unit

        runBlocking {
            metricsProbe.reportEventFailed(EventType.BESKJED, producerName)
        }

        coVerify(exactly = 1) { metricsReporter.registerDataPoint(any(), any(), any()) }
        verify(exactly = 1) { PrometheusMetricsCollector.registerMessageFailed(any(), any()) }

        assertEquals(producerAlias, producerNameForPrometheus.captured)
        assertEquals(producerAlias, capturedTags.captured["producer"])
    }
}