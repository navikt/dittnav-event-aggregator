package no.nav.personbruker.dittnav.eventaggregator.oppgave

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.objectmother.ConsumerRecordsObjectMother
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.invoking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OppgaveEventServiceTest {

    private val repository = mockk<OppgaveRepository>(relaxed = true)
    private val metricsProbe = mockk<EventMetricsProbe>(relaxed = true)
    private val oppgaveService = OppgaveEventService(repository, metricsProbe)

    @BeforeEach
    fun resetMocks() {
        mockkObject(OppgaveTransformer)
        clearMocks(repository)
        clearMocks(metricsProbe)
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
        coVerify(exactly = numberOfFailedTransformations) { metricsProbe.reportEventFailed(any(), any()) }
        capturedStores.size `should be` numberOfSuccessfulTransformations

        confirmVerified(repository)
        confirmVerified(OppgaveTransformer)
    }

    @Test
    fun shouldRegisterMetricsForEveryEvent() {
        val numberOfRecords = 5

        val records = ConsumerRecordsObjectMother.giveMeANumberOfOppgaveRecords(numberOfRecords, "oppgave")

        runBlocking {
            oppgaveService.processEvents(records)
        }

        coVerify (exactly = numberOfRecords) { metricsProbe.reportEventSeen(any(), any()) }
        coVerify (exactly = numberOfRecords) { metricsProbe.reportEventProcessed(any(), any()) }
    }

    private fun createANumberOfTransformedOppgaveRecords(number: Int): List<Oppgave> {
        return (1..number).map {
            OppgaveObjectMother.createOppgave(it.toString(), "12345")
        }
    }
}