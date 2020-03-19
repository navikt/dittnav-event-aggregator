package no.nav.personbruker.dittnav.eventaggregator.innboks

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

class InnboksEventServiceTest {

    private val repository = mockk<InnboksRepository>(relaxed = true)
    private val metricsProbe = mockk<EventMetricsProbe>(relaxed = true)
    private val innboksService = InnboksEventService(repository, metricsProbe)

    @BeforeEach
    fun resetMocks() {
        mockkObject(InnboksTransformer)
        clearMocks(repository)
        clearMocks(metricsProbe)
    }

    @AfterAll
    fun cleanUp() {
        unmockkAll()
    }

    @Test
    fun `Should write events to database`() {
        val records = ConsumerRecordsObjectMother.giveMeANumberOfInnboksRecords(5, "dummyTopic")

        val capturedStores = slot<List<Innboks>>()

        coEvery { repository.writeEventsToCache(capture(capturedStores))} returns Unit

        runBlocking {
            innboksService.processEvents(records)
        }

        verify(exactly = records.count()) { InnboksTransformer.toInternal(any(), any()) }
        coVerify(exactly = 1) { repository.writeEventsToCache(allAny()) }
        capturedStores.captured.size `should be` records.count()

        confirmVerified(repository)
        confirmVerified(InnboksTransformer)
    }

    @Test
    fun `Should finish processing batch before throwing exception when unable to transform event`() {
        val numberOfRecords = 5
        val numberOfFailedTransformations = 1
        val numberOfSuccessfulTransformations = numberOfRecords - numberOfFailedTransformations

        val records = ConsumerRecordsObjectMother.giveMeANumberOfInnboksRecords(numberOfRecords, "dummyTopic")
        val transformedRecords = createANumberOfTransformedInnboksRecords(numberOfSuccessfulTransformations)

        val capturedStores = slot<List<Innboks>>()

        coEvery { repository.writeEventsToCache(capture(capturedStores)) } returns Unit

        val mockedException = UntransformableRecordException("Simulated Exception")

        every { InnboksTransformer.toInternal(any(), any()) } throws mockedException andThenMany transformedRecords

        invoking {
            runBlocking {
                innboksService.processEvents(records)
            }
        } `should throw` UntransformableRecordException::class

        verify(exactly = numberOfRecords) { InnboksTransformer.toInternal(any(), any()) }
        coVerify(exactly = 1) { repository.writeEventsToCache(allAny()) }
        coVerify(exactly = numberOfFailedTransformations) { metricsProbe.reportEventFailed(any(), any()) }
        capturedStores.captured.size `should be` numberOfSuccessfulTransformations

        confirmVerified(repository)
        confirmVerified(InnboksTransformer)
    }

    @Test
    fun shouldRegisterMetricsForEveryEvent() {
        val numberOfRecords = 5

        val records = ConsumerRecordsObjectMother.giveMeANumberOfInnboksRecords(numberOfRecords, "innboks")

        runBlocking {
            innboksService.processEvents(records)
        }

        coVerify (exactly = numberOfRecords) { metricsProbe.reportEventSeen(any(), any()) }
    }

    private fun createANumberOfTransformedInnboksRecords(number: Int): List<Innboks> {
        return (1..number).map {
            InnboksObjectMother.createInnboks(it.toString(), "12345")
        }
    }

}