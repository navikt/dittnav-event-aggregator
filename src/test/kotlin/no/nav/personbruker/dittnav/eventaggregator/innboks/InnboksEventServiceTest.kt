package no.nav.personbruker.dittnav.eventaggregator.innboks

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.objectmother.ConsumerRecordsObjectMother
import no.nav.personbruker.dittnav.eventaggregator.config.*
import no.nav.personbruker.dittnav.eventaggregator.config.PrometheusMetricsCollector.setLifetimeMessagesSeen
import no.nav.personbruker.dittnav.eventaggregator.config.PrometheusMetricsCollector.setMessageLastSeen
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.invoking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InnboksEventServiceTest {

    private val repository = mockk<InnboksRepository>(relaxed = true)
    private val innboksService = InnboksEventService(repository)

    @BeforeEach
    fun resetMocks() {
        mockkObject(InnboksTransformer)
        clearMocks(repository)
    }

    @AfterAll
    fun cleanUp() {
        unmockkAll()
    }

    @Test
    fun `Should write events to database`() {
        val records = ConsumerRecordsObjectMother.giveMeANumberOfInnboksRecords(5, "dummyTopic")

        val capturedStores = ArrayList<Innboks>()

        coEvery { repository.storeInnboksEventInCache(capture(capturedStores))} returns Unit

        runBlocking {
            innboksService.processEvents(records)
        }

        verify(exactly = records.count()) { InnboksTransformer.toInternal(any(), any()) }
        coVerify(exactly = records.count()) { repository.storeInnboksEventInCache(any()) }
        capturedStores.size `should be` records.count()

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

        val capturedStores = ArrayList<Innboks>()

        coEvery { repository.storeInnboksEventInCache(capture(capturedStores)) } returns Unit

        val mockedException = UntransformableRecordException("Simulated Exception")

        every { InnboksTransformer.toInternal(any(), any()) } throws mockedException andThenMany transformedRecords

        invoking {
            runBlocking {
                innboksService.processEvents(records)
            }
        } `should throw` UntransformableRecordException::class

        verify(exactly = numberOfRecords) { InnboksTransformer.toInternal(any(), any()) }
        coVerify(exactly = numberOfSuccessfulTransformations) { repository.storeInnboksEventInCache(any()) }
        capturedStores.size `should be` numberOfSuccessfulTransformations

        confirmVerified(repository)
        confirmVerified(InnboksTransformer)
    }

    @Test
    fun shouldLoadMetricsStateCorrectly() {
        val metrics = listOf(
                MetricsState("innboks", "DittNAV", 2, 5000)
        )

        coEvery{ repository.getInnboksMetricsState() } returns metrics

        val capturedCount = CapturingSlot<Int>()
        val capturedLastSeen = CapturingSlot<Long>()

        mockkObject( PrometheusMetricsCollector )

        every { setLifetimeMessagesSeen("innboks", "DittNAV", capture(capturedCount)) } returns Unit
        every { setMessageLastSeen("innboks", "DittNAV", capture(capturedLastSeen)) } returns Unit

        innboksService.initInnboksMetrics()

        capturedCount.captured `should equal` 2
        capturedLastSeen.captured `should equal` 5000
    }

    @Test
    fun shouldRegisterMetricsForEveryEvent() {
        val numberOfRecords = 5

        val records = ConsumerRecordsObjectMother.giveMeANumberOfInnboksRecords(numberOfRecords, "innboks")

        mockkObject( PrometheusMetricsCollector )

        runBlocking {
            innboksService.processEvents(records)
        }

        verify (exactly = numberOfRecords) { PrometheusMetricsCollector.registerMessageSeen(any(), any()) }
    }

    private fun createANumberOfTransformedInnboksRecords(number: Int): List<Innboks> {
        return (1..number).map {
            InnboksObjectMother.createInnboks(it.toString(), "12345")
        }
    }

}