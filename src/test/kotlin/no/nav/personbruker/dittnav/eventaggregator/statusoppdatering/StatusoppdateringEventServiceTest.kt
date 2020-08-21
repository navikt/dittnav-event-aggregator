package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonPersistingService
import no.nav.personbruker.dittnav.eventaggregator.common.emptyPersistResult
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.common.objectmother.ConsumerRecordsObjectMother
import no.nav.personbruker.dittnav.eventaggregator.common.successfulEvents
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsSession
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.invoking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class StatusoppdateringEventServiceTest {

    private val persistingService = mockk<BrukernotifikasjonPersistingService<Statusoppdatering>>(relaxed = true)
    private val metricsProbe = mockk<EventMetricsProbe>(relaxed = true)
    private val metricsSession = mockk<EventMetricsSession>(relaxed = true)
    private val eventService = StatusoppdateringEventService(persistingService, metricsProbe)

    @BeforeEach
    private fun resetMocks() {
        mockkObject(StatusoppdateringTransformer)
        clearMocks(persistingService)
        clearMocks(metricsProbe)
        clearMocks(metricsSession)
    }

    @AfterAll
    private fun cleanUp() {
        unmockkAll()
    }

    @Test
    fun `Skal skrive alle eventer til databasen`() {
        val records = ConsumerRecordsObjectMother.giveMeANumberOfStatusoppdateringRecords(5, "dummyTopic")

        val capturedListOfEntities = slot<List<Statusoppdatering>>()
        coEvery { persistingService.writeEventsToCache(capture(capturedListOfEntities)) } returns emptyPersistResult()

        val slot = slot<suspend EventMetricsSession.() -> Unit>()

        coEvery { metricsProbe.runWithMetrics(any(), capture(slot)) } coAnswers {
            slot.captured.invoke(metricsSession)
        }

        runBlocking {
            eventService.processEvents(records)
        }

        verify(exactly = records.count()) { StatusoppdateringTransformer.toInternal(any(), any()) }
        coVerify(exactly = 1) { persistingService.writeEventsToCache(allAny()) }
        capturedListOfEntities.captured.size `should be` records.count()

        confirmVerified(StatusoppdateringTransformer)
        confirmVerified(persistingService)
    }

    @Test
    fun `Skal haandtere at enkelte transformasjoner feiler og fortsette aa transformere batch-en, for det til slutt kastes en exception`() {
        val totalNumberOfRecords = 5
        val numberOfFailedTransformations = 1
        val numberOfSuccessfulTransformations = totalNumberOfRecords - numberOfFailedTransformations

        val records = ConsumerRecordsObjectMother.giveMeANumberOfStatusoppdateringRecords(totalNumberOfRecords, "dummyTopic")
        val transformedRecords = createANumberOfTransformedRecords(numberOfSuccessfulTransformations)

        val persistResult = successfulEvents(transformedRecords)

        val capturedListOfEntities = slot<List<Statusoppdatering>>()
        coEvery { persistingService.writeEventsToCache(capture(capturedListOfEntities)) } returns persistResult

        val retriableExp = UntransformableRecordException("Simulert feil i en test")
        every { StatusoppdateringTransformer.toInternal(any(), any()) } throws retriableExp andThenMany transformedRecords

        val slot = slot<suspend EventMetricsSession.() -> Unit>()

        coEvery { metricsProbe.runWithMetrics(any(), capture(slot)) } coAnswers {
            slot.captured.invoke(metricsSession)
        }

        invoking {
            runBlocking {
                eventService.processEvents(records)
            }
        } `should throw` UntransformableRecordException::class

        coVerify(exactly = totalNumberOfRecords) { StatusoppdateringTransformer.toInternal(any(), any()) }
        coVerify(exactly = 1) { persistingService.writeEventsToCache(allAny()) }
        coVerify(exactly = numberOfFailedTransformations) { metricsSession.countFailedEventForProducer(any()) }
        capturedListOfEntities.captured.size `should be` numberOfSuccessfulTransformations

        confirmVerified(StatusoppdateringTransformer)
        confirmVerified(persistingService)
    }

    @Test
    fun shouldReportEverySuccessfulEvent() {
        val numberOfRecords = 5

        val records = ConsumerRecordsObjectMother.giveMeANumberOfStatusoppdateringRecords(numberOfRecords, "statusoppdatering")
        val slot = slot<suspend EventMetricsSession.() -> Unit>()

        coEvery { persistingService.writeEventsToCache(any()) } returns emptyPersistResult()

        coEvery { metricsProbe.runWithMetrics(any(), capture(slot)) } coAnswers {
            slot.captured.invoke(metricsSession)
        }

        runBlocking {
            eventService.processEvents(records)
        }

        coVerify(exactly = numberOfRecords) { metricsSession.countSuccessfulEventForProducer(any()) }
    }

    @Test
    fun `skal forkaste eventer som har valideringsfeil`() {
        val statusoppdateringWithTooLongStatusInternField = AvroStatusoppdateringObjectMother.createStatusoppdateringWithStatusIntern("S".repeat(101))
        val cr = ConsumerRecordsObjectMother.createConsumerRecord("statusoppdatering", statusoppdateringWithTooLongStatusInternField)
        val records = ConsumerRecordsObjectMother.giveMeConsumerRecordsWithThisConsumerRecord(cr)

        val slot = slot<suspend EventMetricsSession.() -> Unit>()
        coEvery { metricsProbe.runWithMetrics(any(), capture(slot)) } coAnswers {
            slot.captured.invoke(metricsSession)
        }

        val capturedNumberOfEntitiesWrittenToTheDb = slot<List<Statusoppdatering>>()
        coEvery { persistingService.writeEventsToCache(capture(capturedNumberOfEntitiesWrittenToTheDb)) } returns emptyPersistResult()

        runBlocking {
            eventService.processEvents(records)
        }

        capturedNumberOfEntitiesWrittenToTheDb.captured.size `should be` 0

        coVerify(exactly = 1) { metricsSession.countFailedEventForProducer(any()) }
    }

    private fun createANumberOfTransformedRecords(numberOfRecords: Int): MutableList<Statusoppdatering> {
        val transformedRecords = mutableListOf<Statusoppdatering>()
        for (i in 0 until numberOfRecords) {
            transformedRecords.add(StatusoppdateringObjectMother.giveMeStatusoppdatering("$i", "{$i}12345"))
        }
        return transformedRecords
    }

}
