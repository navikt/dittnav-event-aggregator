package no.nav.personbruker.dittnav.eventaggregator.beskjed

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.common.objectmother.ConsumerRecordsObjectMother
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.PrometheusMetricsCollector
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.invoking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BeskjedEventServiceTest {

    private val beskjedRepository = mockk<BeskjedRepository>(relaxed = true)
    private val eventMetricsProbe = mockk<EventMetricsProbe>(relaxed = true)
    private val eventService = BeskjedEventService(beskjedRepository, eventMetricsProbe)

    @BeforeEach
    private fun resetMocks() {
        mockkObject(BeskjedTransformer)
        clearMocks(beskjedRepository)
        clearMocks(eventMetricsProbe)
    }

    @AfterAll
    private fun cleanUp() {
        unmockkAll()
    }

    @Test
    fun `Skal skrive alle eventer til databasen`() {
        val records = ConsumerRecordsObjectMother.giveMeANumberOfBeskjedRecords(5, "dummyTopic")

        val capturedListOfEntities = slot<List<Beskjed>>()
        coEvery { beskjedRepository.writeEventsToCache(capture(capturedListOfEntities)) } returns Unit

        runBlocking {
            eventService.processEvents(records)
        }

        verify(exactly = records.count()) { BeskjedTransformer.toInternal(any(), any()) }
        coVerify(exactly = 1) { beskjedRepository.writeEventsToCache(allAny()) }
        capturedListOfEntities.captured.size `should be` records.count()

        confirmVerified(BeskjedTransformer)
        confirmVerified(beskjedRepository)
    }

    @Test
    fun `Skal haandtere at enkelte transformasjoner feiler og fortsette aa transformere batch-en, for det til slutt kastes en exception`() {
        val totalNumberOfRecords = 5
        val numberOfFailedTransformations = 1
        val numberOfSuccessfulTransformations = totalNumberOfRecords - numberOfFailedTransformations

        val records = ConsumerRecordsObjectMother.giveMeANumberOfBeskjedRecords(totalNumberOfRecords, "dummyTopic")
        val transformedRecords = createANumberOfTransformedRecords(numberOfSuccessfulTransformations)

        val capturedListOfEntities = slot<List<Beskjed>>()
        coEvery { beskjedRepository.writeEventsToCache(capture(capturedListOfEntities)) } returns Unit

        val retriableExp = UntransformableRecordException("Simulert feil i en test")
        every { BeskjedTransformer.toInternal(any(), any()) } throws retriableExp andThenMany transformedRecords

        invoking {
            runBlocking {
                eventService.processEvents(records)
            }
        } `should throw` UntransformableRecordException::class

        coVerify(exactly = totalNumberOfRecords) { BeskjedTransformer.toInternal(any(), any()) }
        coVerify(exactly = 1) { beskjedRepository.writeEventsToCache(allAny()) }
        coVerify(exactly = numberOfFailedTransformations) { eventMetricsProbe.reportEventFailed(any(), any()) }
        capturedListOfEntities.captured.size `should be` numberOfSuccessfulTransformations

        confirmVerified(BeskjedTransformer)
        confirmVerified(beskjedRepository)
    }

    @Test
    fun shouldRegisterMetricsForEveryEvent() {
        val numberOfRecords = 5

        val records = ConsumerRecordsObjectMother.giveMeANumberOfBeskjedRecords(numberOfRecords, "beskjed")

        mockkObject(PrometheusMetricsCollector)

        runBlocking {
            eventService.processEvents(records)
        }

        coVerify (exactly = numberOfRecords) { eventMetricsProbe.reportEventSeen(any(), any()) }
        coVerify (exactly = numberOfRecords) { eventMetricsProbe.reportEventProcessed(any(), any()) }
    }

    private fun createANumberOfTransformedRecords(numberOfRecords: Int): MutableList<Beskjed> {
        val transformedRecords = mutableListOf<Beskjed>()
        for (i in 0 until numberOfRecords) {
            transformedRecords.add(BeskjedObjectMother.giveMeAktivBeskjed("$i", "{$i}12345"))
        }
        return transformedRecords
    }

}
