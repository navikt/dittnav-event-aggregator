package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.objectmother.ConsumerRecordsObjectMother
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusObjectMother.createDoknotifikasjonStatus
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.metrics.DoknotifikasjonStatusMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.metrics.DoknotifikasjonStatusMetricsSession
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DoknotifikasjonStatusServiceTest {

    private val metricsSession: DoknotifikasjonStatusMetricsSession = mockk()
    private val metricsProbe: DoknotifikasjonStatusMetricsProbe = mockk()
    private val statusUpdater: DoknotifikasjonStatusUpdater = mockk()

    private val doknotifikasjonStatusService = DoknotifikasjonStatusService(statusUpdater, metricsProbe)

    private val statusMatchingBeskjed = createDoknotifikasjonStatus("beskjed")
    private val statusMatchingOppgave = createDoknotifikasjonStatus("oppgave")
    private val statusMatchingNone = createDoknotifikasjonStatus("none")

    private val allStatuses = listOf(statusMatchingBeskjed, statusMatchingOppgave, statusMatchingNone)
    private val statusEvents = ConsumerRecordsObjectMother.doknotStatusesAsConsumerRecords(allStatuses, "doknot")

    @BeforeEach
    fun setupMocks() {
        val block = slot<suspend DoknotifikasjonStatusMetricsSession.() -> Unit>()

        coEvery {
            metricsProbe.runWithMetrics(capture(block))
        } coAnswers {
            block.captured(metricsSession)
        }

        every {
            metricsSession.countStatuses(any())
        } returns Unit

        every {
            metricsSession.recordUpdateResult(any(), any())
        } returns Unit
    }

    @Test
    fun `should process status-events and report number updated for each type`() {

        val updateResultBeskjed: UpdateStatusResult = mockk()
        val updateResultOppgave: UpdateStatusResult = mockk()

        coEvery {
            statusUpdater.updateStatusForBeskjed(allStatuses)
        } returns updateResultBeskjed

        coEvery {
            statusUpdater.updateStatusForOppgave(allStatuses)
        } returns updateResultOppgave

        runBlocking {
            doknotifikasjonStatusService.processEvents(statusEvents)
        }

        verify(exactly = 1) { metricsSession.countStatuses(allStatuses) }
        verify(exactly = 1) { metricsSession.recordUpdateResult(EventType.BESKJED_INTERN, updateResultBeskjed) }
        verify(exactly = 1) { metricsSession.recordUpdateResult(EventType.OPPGAVE_INTERN, updateResultOppgave) }
    }
}
