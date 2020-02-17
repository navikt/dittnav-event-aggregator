package no.nav.personbruker.dittnav.eventaggregator.oppgave

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.objectmother.ConsumerRecordsObjectMother
import no.nav.personbruker.dittnav.eventaggregator.config.MetricsState
import no.nav.personbruker.dittnav.eventaggregator.config.PrometheusMetricsCollector
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.invoking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OppgaveEventServiceTest {

    private val repository = mockk<OppgaveRepository>(relaxed = true)
    private val oppgaveService = OppgaveEventService(repository)

    @BeforeEach
    fun resetMocks() {
        mockkObject(OppgaveTransformer)
        clearMocks(repository)
    }

    @AfterAll
    fun cleanUp() {
        unmockkAll()
    }

    @Test
    fun `Should write events to database`() {
        val records = ConsumerRecordsObjectMother.giveMeANumberOfOppgaveRecords(5, "dummyTopic")

        val capturedStores = ArrayList<Oppgave>()

        coEvery { repository.storeOppgaveEventInCache(capture(capturedStores))} returns Unit

        runBlocking {
            oppgaveService.processEvents(records)
        }

        verify(exactly = records.count()) { OppgaveTransformer.toInternal(any(), any()) }
        coVerify(exactly = records.count()) { repository.storeOppgaveEventInCache(any()) }
        capturedStores.size `should be` records.count()

        confirmVerified(repository)
        confirmVerified(OppgaveTransformer)
    }

    @Test
    fun `Should finish processing batch before throwing exception when unable to transform event`() {
        val numberOfRecords = 5
        val numberOfFailedTransformations = 1
        val numberOfSuccessfulTransformations = numberOfRecords - numberOfFailedTransformations

        val records = ConsumerRecordsObjectMother.giveMeANumberOfOppgaveRecords(numberOfRecords, "dummyTopic")
        val transformedRecords = createANumberOfTransformedOppgaveRecords(numberOfSuccessfulTransformations)

        val capturedStores = ArrayList<Oppgave>()

        coEvery { repository.storeOppgaveEventInCache(capture(capturedStores)) } returns Unit

        val mockedException = UntransformableRecordException("Simulated Exception")

        every { OppgaveTransformer.toInternal(any(), any()) } throws mockedException andThenMany transformedRecords

        invoking {
            runBlocking {
                oppgaveService.processEvents(records)
            }
        } `should throw` UntransformableRecordException::class

        verify(exactly = numberOfRecords) { OppgaveTransformer.toInternal(any(), any()) }
        coVerify(exactly = numberOfSuccessfulTransformations) { repository.storeOppgaveEventInCache(any()) }
        capturedStores.size `should be` numberOfSuccessfulTransformations

        confirmVerified(repository)
        confirmVerified(OppgaveTransformer)
    }

    @Test
    fun shouldLoadMetricsStateCorrectly() {
        val metrics = listOf(
                MetricsState("oppgave", "DittNAV", 2, 5000)
        )

        coEvery{ repository.getOppgaveMetricsState() } returns metrics

        val capturedCount = CapturingSlot<Int>()
        val capturedLastSeen = CapturingSlot<Long>()

        mockkObject( PrometheusMetricsCollector )

        every { PrometheusMetricsCollector.setLifetimeMessagesSeen("oppgave", "DittNAV", capture(capturedCount)) } returns Unit
        every { PrometheusMetricsCollector.setMessageLastSeen("oppgave", "DittNAV", capture(capturedLastSeen)) } returns Unit

        oppgaveService.initOppgaveMetrics()

        capturedCount.captured `should equal` 2
        capturedLastSeen.captured `should equal` 5000
    }

    @Test
    fun shouldRegisterMetricsForEveryEvent() {
        val numberOfRecords = 5

        val records = ConsumerRecordsObjectMother.giveMeANumberOfOppgaveRecords(numberOfRecords, "oppgave")

        mockkObject( PrometheusMetricsCollector )

        runBlocking {
            oppgaveService.processEvents(records)
        }

        verify (exactly = numberOfRecords) { PrometheusMetricsCollector.registerMessageSeen(any(), any()) }
    }

    private fun createANumberOfTransformedOppgaveRecords(number: Int): List<Oppgave> {
        return (1..number).map {
            OppgaveObjectMother.createOppgave(it.toString(), "12345")
        }
    }
}