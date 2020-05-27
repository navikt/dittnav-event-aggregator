package no.nav.personbruker.dittnav.eventaggregator.metrics

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.DBMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.influx.EVENTS_CACHED
import org.junit.jupiter.api.Test

class DBMetricsProbeTest {

    private val metricsReporter = mockk<MetricsReporter>()
    private val prometheusCollector = mockkObject(PrometheusMetricsCollector)
    private val dbMetricsProbe = DBMetricsProbe(metricsReporter)

    @Test
    fun `skal telle riktig antall eventer av gitt type i cache`() {
        coEvery { metricsReporter.registerDataPoint(EVENTS_CACHED, any(), any()) } returns Unit
        every { PrometheusMetricsCollector.registerEventsCached(any(), EventType.DONE) } returns Unit
        runBlocking {
            dbMetricsProbe.numberOfCachedEventsOfType(4, EventType.DONE)
        }
        coVerify(exactly = 1) {
            metricsReporter.registerDataPoint(EVENTS_CACHED, mapOf("numberOfEvents" to 4), mapOf("eventType" to EventType.DONE.eventType))
        }
    }
}
