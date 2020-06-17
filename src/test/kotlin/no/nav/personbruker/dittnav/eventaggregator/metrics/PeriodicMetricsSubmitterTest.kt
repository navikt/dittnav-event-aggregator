package no.nav.personbruker.dittnav.eventaggregator.metrics

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.count.DbEventCounterService
import no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.topic.TopicEventCounterService
import no.nav.personbruker.dittnav.eventaggregator.metrics.submitter.PeriodicMetricsSubmitter
import org.junit.jupiter.api.Test

internal class PeriodicMetricsSubmitterTest {

    @Test
    fun `Should report metrics for both kafka topics and the database cache`() {
        val topicEventCounterService = mockk<TopicEventCounterService>(relaxed = true)
        val dbEventCounterService = mockk<DbEventCounterService>(relaxed = true)

        val submitter = PeriodicMetricsSubmitter(dbEventCounterService, topicEventCounterService)

        runBlocking {
            submitter.submitMetrics()
        }

        coVerify(exactly = 1) { topicEventCounterService.countEventsAndReportMetrics() }
        coVerify(exactly = 1) { dbEventCounterService.countEventsAndReportMetrics() }

        confirmVerified(topicEventCounterService)
        confirmVerified(dbEventCounterService)
    }

}
