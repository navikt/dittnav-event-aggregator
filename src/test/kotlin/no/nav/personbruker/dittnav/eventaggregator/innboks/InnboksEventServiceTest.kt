package no.nav.personbruker.dittnav.eventaggregator.innboks

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
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsSession
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InnboksEventServiceTest {

    private val persistingService = mockk<BrukernotifikasjonPersistingService<Innboks>>(relaxed = true)
    private val metricsProbe = mockk<EventMetricsProbe>(relaxed = true)
    private val metricsSession = mockk<EventMetricsSession>(relaxed = true)
    private val eventService = InnboksEventService(persistingService, metricsProbe)

    @BeforeEach
    fun resetMocks() {
        mockkObject(InnboksTransformer)
        clearMocks(persistingService)
        clearMocks(metricsProbe)
        clearMocks(metricsSession)
    }

    @AfterAll
    fun cleanUp() {
        unmockkAll()
    }

    @Test
    fun `Should write events to database`() {
        val records = ConsumerRecordsObjectMother.giveMeANumberOfInnboksRecords(5, "dummyTopic")

        val capturedStores = slot<List<Innboks>>()

        coEvery { persistingService.writeEventsToCache(capture(capturedStores)) } returns emptyPersistResult()

        val slot = slot<suspend EventMetricsSession.() -> Unit>()

        coEvery { metricsProbe.runWithMetrics(any(), capture(slot)) } coAnswers {
            slot.captured.invoke(metricsSession)
        }

        runBlocking {
            eventService.processEvents(records)
        }

        verify(exactly = records.count()) { InnboksTransformer.toInternal(any(), any()) }
        coVerify(exactly = 1) { persistingService.writeEventsToCache(allAny()) }
        capturedStores.captured.size shouldBe records.count()

        confirmVerified(persistingService)
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

        coEvery { persistingService.writeEventsToCache(capture(capturedStores)) } returns emptyPersistResult()

        val mockedException = UntransformableRecordException("Simulated Exception")

        every { InnboksTransformer.toInternal(any(), any()) } throws mockedException andThenMany transformedRecords

        val slot = slot<suspend EventMetricsSession.() -> Unit>()

        coEvery { metricsProbe.runWithMetrics(any(), capture(slot)) } coAnswers {
            slot.captured.invoke(metricsSession)
        }

        shouldThrow<UntransformableRecordException> {
            runBlocking {
                eventService.processEvents(records)
            }
        }

        verify(exactly = numberOfRecords) { InnboksTransformer.toInternal(any(), any()) }
        coVerify(exactly = 1) { persistingService.writeEventsToCache(allAny()) }
        coVerify(exactly = numberOfFailedTransformations) { metricsSession.countFailedEventForProducer(any()) }
        capturedStores.captured.size shouldBe numberOfSuccessfulTransformations

        confirmVerified(persistingService)
        confirmVerified(InnboksTransformer)
    }

    @Test
    fun shouldReportEverySuccessfulEvent() {
        val numberOfRecords = 5

        val records = ConsumerRecordsObjectMother.giveMeANumberOfInnboksRecords(numberOfRecords, "innboks")

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

    private fun createANumberOfTransformedInnboksRecords(number: Int): List<Innboks> {
        return (1..number).map {
            InnboksObjectMother.giveMeAktivInnboks(it.toString(), "12345")
        }
    }

}