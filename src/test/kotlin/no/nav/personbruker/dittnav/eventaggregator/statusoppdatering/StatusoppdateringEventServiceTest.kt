package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonPersistingService
import no.nav.personbruker.dittnav.eventaggregator.common.emptyPersistResult
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.common.objectmother.ConsumerRecordsObjectMother
import no.nav.personbruker.dittnav.eventaggregator.common.successfulEvents
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsSession
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
        capturedListOfEntities.captured.size shouldBe records.count()

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

        shouldThrow<UntransformableRecordException> {
            runBlocking {
                eventService.processEvents(records)
            }
        }

        coVerify(exactly = totalNumberOfRecords) { StatusoppdateringTransformer.toInternal(any(), any()) }
        coVerify(exactly = 1) { persistingService.writeEventsToCache(allAny()) }
        coVerify(exactly = numberOfFailedTransformations) { metricsSession.countFailedEventForProducer(any()) }
        capturedListOfEntities.captured.size shouldBe numberOfSuccessfulTransformations

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

    private fun createANumberOfTransformedRecords(numberOfRecords: Int): MutableList<Statusoppdatering> {
        val transformedRecords = mutableListOf<Statusoppdatering>()
        for (i in 0 until numberOfRecords) {
            transformedRecords.add(StatusoppdateringObjectMother.giveMeStatusoppdatering("$i", "{$i}12345"))
        }
        return transformedRecords
    }

}
