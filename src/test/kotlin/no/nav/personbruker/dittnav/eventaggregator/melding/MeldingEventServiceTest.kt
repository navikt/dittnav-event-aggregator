package no.nav.personbruker.dittnav.eventaggregator.melding

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.objectmother.ConsumerRecordsObjectMother
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.invoking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MeldingEventServiceTest {

    private val repository = mockk<MeldingRepository>(relaxed = true)
    private val meldingService = MeldingEventService(repository)

    @BeforeEach
    fun resetMocks() {
        mockkObject(MeldingTransformer)
        clearMocks(repository)
    }

    @AfterAll
    fun cleanUp() {
        unmockkAll()
    }

    @Test
    fun `Should write events to database`() {
        val records = ConsumerRecordsObjectMother.giveMeANumberOfMeldingRecords(5, "dummyTopic")

        val capturedStores = ArrayList<Melding>()

        coEvery { repository.storeMeldingEventInCache(capture(capturedStores))} returns Unit

        runBlocking {
            meldingService.processEvents(records)
        }

        verify(exactly = records.count()) { MeldingTransformer.toInternal(any()) }
        coVerify(exactly = records.count()) { repository.storeMeldingEventInCache(any()) }
        capturedStores.size `should be` records.count()

        confirmVerified(repository)
        confirmVerified(MeldingTransformer)
    }

    @Test
    fun `Should finish processing batch before throwing exception when unable to transform event`() {
        val numberOfRecords = 5
        val numberOfFailedTransformations = 1
        val numberOfSuccessfulTransformations = numberOfRecords - numberOfFailedTransformations

        val records = ConsumerRecordsObjectMother.giveMeANumberOfMeldingRecords(numberOfRecords, "dummyTopic")
        val transformedRecords = createANumberOfTransformedMeldingRecords(numberOfSuccessfulTransformations)

        val capturedStores = ArrayList<Melding>()

        coEvery { repository.storeMeldingEventInCache(capture(capturedStores)) } returns Unit

        val mockedException = UntransformableRecordException("Simulated Exception")

        every { MeldingTransformer.toInternal(any()) } throws mockedException andThenMany transformedRecords

        invoking {
            runBlocking {
                meldingService.processEvents(records)
            }
        } `should throw` UntransformableRecordException::class

        verify(exactly = numberOfRecords) { MeldingTransformer.toInternal(any()) }
        coVerify(exactly = numberOfSuccessfulTransformations) { repository.storeMeldingEventInCache(any()) }
        capturedStores.size `should be` numberOfSuccessfulTransformations

        confirmVerified(repository)
        confirmVerified(MeldingTransformer)
    }

    private fun createANumberOfTransformedMeldingRecords(number: Int): List<Melding> {
        return (1..number).map {
            MeldingObjectMother.createMelding(it.toString(), "12345")
        }
    }

}