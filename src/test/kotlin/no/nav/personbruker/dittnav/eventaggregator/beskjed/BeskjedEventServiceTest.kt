package no.nav.personbruker.dittnav.eventaggregator.beskjed

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.common.objectmother.ConsumerRecordsObjectMother
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsSession
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.invoking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BeskjedEventServiceTest {

    private val beskjedService = mockk<BeskjedDatabaseService>(relaxed = true)
    private val metricsProbe = mockk<EventMetricsProbe>(relaxed = true)
    private val metricsSession = mockk<EventMetricsSession>(relaxed = true)
    private val beskjedEventService = BeskjedEventService(beskjedService, metricsProbe)

    @BeforeEach
    private fun resetMocks() {
        mockkObject(BeskjedTransformer)
        clearMocks(beskjedService)
        clearMocks(metricsProbe)
        clearMocks(metricsSession)
    }

    @AfterAll
    private fun cleanUp() {
        unmockkAll()
    }

    @Test
    fun `Skal skrive alle eventer til databasen`() {
        val records = ConsumerRecordsObjectMother.giveMeANumberOfBeskjedRecords(5, "dummyTopic")

        val capturedListOfEntities = slot<List<Beskjed>>()
        coEvery { beskjedService.writeEventsToCache(capture(capturedListOfEntities)) } returns Unit

        val slot = slot<suspend EventMetricsSession.() -> Unit>()

        coEvery { metricsProbe.runWithMetrics(any(), capture(slot)) } coAnswers {
            slot.captured.invoke(metricsSession)
        }

        runBlocking {
            beskjedEventService.processEvents(records)
        }

        verify(exactly = records.count()) { BeskjedTransformer.toInternal(any(), any()) }
        coVerify(exactly = 1) { beskjedService.writeEventsToCache(allAny()) }
        capturedListOfEntities.captured.size `should be` records.count()

        confirmVerified(BeskjedTransformer)
        confirmVerified(beskjedService)
    }

    @Test
    fun `Skal haandtere at enkelte transformasjoner feiler og fortsette aa transformere batch-en, for det til slutt kastes en exception`() {
        val totalNumberOfRecords = 5
        val numberOfFailedTransformations = 1
        val numberOfSuccessfulTransformations = totalNumberOfRecords - numberOfFailedTransformations

        val records = ConsumerRecordsObjectMother.giveMeANumberOfBeskjedRecords(totalNumberOfRecords, "dummyTopic")
        val transformedRecords = createANumberOfTransformedRecords(numberOfSuccessfulTransformations)

        val capturedListOfEntities = slot<List<Beskjed>>()
        coEvery { beskjedService.writeEventsToCache(capture(capturedListOfEntities)) } returns Unit

        val retriableExp = UntransformableRecordException("Simulert feil i en test")
        every { BeskjedTransformer.toInternal(any(), any()) } throws retriableExp andThenMany transformedRecords

        val slot = slot<suspend EventMetricsSession.() -> Unit>()

        coEvery { metricsProbe.runWithMetrics(any(), capture(slot)) } coAnswers {
            slot.captured.invoke(metricsSession)
        }

        invoking {
            runBlocking {
                beskjedEventService.processEvents(records)
            }
        } `should throw` UntransformableRecordException::class

        coVerify(exactly = totalNumberOfRecords) { BeskjedTransformer.toInternal(any(), any()) }
        coVerify(exactly = 1) { beskjedService.writeEventsToCache(allAny()) }
        coVerify(exactly = numberOfFailedTransformations) { metricsSession.countFailedEventForProducer(any()) }
        capturedListOfEntities.captured.size `should be` numberOfSuccessfulTransformations

        confirmVerified(BeskjedTransformer)
        confirmVerified(beskjedService)
    }

    @Test
    fun shouldReportEverySuccessfulEvent() {
        val numberOfRecords = 5

        val records = ConsumerRecordsObjectMother.giveMeANumberOfBeskjedRecords(numberOfRecords, "beskjed")
        val slot = slot<suspend EventMetricsSession.() -> Unit>()

        coEvery { metricsProbe.runWithMetrics(any(), capture(slot)) } coAnswers {
            slot.captured.invoke(metricsSession)
        }

        runBlocking {
            beskjedEventService.processEvents(records)
        }

        coVerify (exactly = numberOfRecords) { metricsSession.countSuccessfulEventForProducer(any()) }
    }

    @Test
    fun `skal forkaste eventer som mangler tekst i tekstfeltet`() {
        val beskjedWithoutText = AvroBeskjedObjectMother.createBeskjedWithText("")
        val cr = ConsumerRecordsObjectMother.createConsumerRecord("beskjed", beskjedWithoutText)
        val records = ConsumerRecordsObjectMother.giveMeConsumerRecordsWithThisConsumerRecord(cr)

        val slot = slot<suspend EventMetricsSession.() -> Unit>()
        coEvery { metricsProbe.runWithMetrics(any(), capture(slot)) } coAnswers {
            slot.captured.invoke(metricsSession)
        }

        val capturedNumberOfEntitiesWrittenToTheDb = slot<List<Beskjed>>()
        coEvery { beskjedService.writeEventsToCache(capture(capturedNumberOfEntitiesWrittenToTheDb)) } returns Unit

        runBlocking {
            beskjedEventService.processEvents(records)
        }

        capturedNumberOfEntitiesWrittenToTheDb.captured.size `should be` 0

        coVerify (exactly = 1) { metricsSession.countFailedEventForProducer(any()) }
    }

    @Test
    fun `skal forkaste eventer som har valideringsfeil`() {
        val tooLongText = "A".repeat(501)
        val beskjedWithTooLongText = AvroBeskjedObjectMother.createBeskjedWithText(tooLongText)
        val cr = ConsumerRecordsObjectMother.createConsumerRecord("beskjed", beskjedWithTooLongText)
        val records = ConsumerRecordsObjectMother.giveMeConsumerRecordsWithThisConsumerRecord(cr)

        val slot = slot<suspend EventMetricsSession.() -> Unit>()
        coEvery { metricsProbe.runWithMetrics(any(), capture(slot)) } coAnswers {
            slot.captured.invoke(metricsSession)
        }

        val capturedNumberOfEntitiesWrittenToTheDb = slot<List<Beskjed>>()
        coEvery { beskjedService.writeEventsToCache(capture(capturedNumberOfEntitiesWrittenToTheDb)) } returns Unit

        runBlocking {
            beskjedEventService.processEvents(records)
        }

        capturedNumberOfEntitiesWrittenToTheDb.captured.size `should be` 0

        coVerify (exactly = 1) { metricsSession.countFailedEventForProducer(any()) }
    }

    private fun createANumberOfTransformedRecords(numberOfRecords: Int): MutableList<Beskjed> {
        val transformedRecords = mutableListOf<Beskjed>()
        for (i in 0 until numberOfRecords) {
            transformedRecords.add(BeskjedObjectMother.giveMeAktivBeskjed("$i", "{$i}12345"))
        }
        return transformedRecords
    }

}
