package no.nav.personbruker.dittnav.eventaggregator.informasjon

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

class InformasjonEventServiceTest {

    private val informasjonRepository = mockk<InformasjonRepository>(relaxed = true)
    private val eventService = InformasjonEventService(informasjonRepository)

    @BeforeEach
    private fun resetMocks() {
        mockkObject(InformasjonTransformer)
        clearMocks(informasjonRepository)
    }

    @AfterAll
    private fun cleanUp() {
        unmockkAll()
    }

    @Test
    fun `Skal skrive alle eventer til databasen`() {
        val records = ConsumerRecordsObjectMother.giveMeANumberOfInformationRecords(5, "dummyTopic")

        val capturedListOfEntities = slot<List<Informasjon>>()
        coEvery { informasjonRepository.writeEventsToCache(capture(capturedListOfEntities)) } returns Unit

        runBlocking {
            eventService.processEvents(records)
        }

        verify(exactly = records.count()) { InformasjonTransformer.toInternal(any()) }
        coVerify(exactly = 1) { informasjonRepository.writeEventsToCache(allAny()) }
        capturedListOfEntities.captured.size `should be` records.count()

        confirmVerified(InformasjonTransformer)
        confirmVerified(informasjonRepository)
    }

    @Test
    fun `Skal haandtere at enkelte transformasjoner feiler og fortsette aa transformere batch-en, for det til slutt kastes en exception`() {
        val totalNumberOfRecords = 5
        val numberOfFailedTransformations = 1
        val numberOfSuccessfulTransformations = totalNumberOfRecords - numberOfFailedTransformations

        val records = ConsumerRecordsObjectMother.giveMeANumberOfInformationRecords(totalNumberOfRecords, "dummyTopic")
        val transformedRecords = createANumberOfTransformedRecords(numberOfSuccessfulTransformations)

        val capturedListOfEntities = slot<List<Informasjon>>()
        coEvery { informasjonRepository.writeEventsToCache(capture(capturedListOfEntities)) } returns Unit

        val retriableExp = UntransformableRecordException("Simulert feil i en test")
        every { InformasjonTransformer.toInternal(any()) } throws retriableExp andThenMany transformedRecords

        invoking {
            runBlocking {
                eventService.processEvents(records)
            }
        } `should throw` UntransformableRecordException::class

        coVerify(exactly = totalNumberOfRecords) { InformasjonTransformer.toInternal(any()) }
        coVerify(exactly = 1) { informasjonRepository.writeEventsToCache(allAny()) }
        capturedListOfEntities.captured.size `should be` numberOfSuccessfulTransformations

        confirmVerified(InformasjonTransformer)
        confirmVerified(informasjonRepository)
    }

    private fun createANumberOfTransformedRecords(numberOfRecords: Int): MutableList<Informasjon> {
        val transformedRecords = mutableListOf<Informasjon>()
        for (i in 0 until numberOfRecords) {
            transformedRecords.add(InformasjonObjectMother.createInformasjon("$i", "{$i}12345"))
        }
        return transformedRecords
    }

}
