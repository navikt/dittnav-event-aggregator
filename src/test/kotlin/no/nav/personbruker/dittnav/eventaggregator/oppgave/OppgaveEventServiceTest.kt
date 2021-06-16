package no.nav.personbruker.dittnav.eventaggregator.oppgave

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Beskjed
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonPersistingService
import no.nav.personbruker.dittnav.eventaggregator.common.emptyPersistResult
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.common.objectmother.ConsumerRecordsObjectMother
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsSession
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.invoking
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OppgaveEventServiceTest {

    private val persistingService = mockk<BrukernotifikasjonPersistingService<Oppgave>>(relaxed = true)
    private val metricsProbe = mockk<EventMetricsProbe>(relaxed = true)
    private val metricsSession = mockk<EventMetricsSession>(relaxed = true)
    private val eventService = OppgaveEventService(persistingService, metricsProbe)

    @BeforeEach
    fun resetMocks() {
        mockkObject(OppgaveTransformer)
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
        val records = ConsumerRecordsObjectMother.giveMeANumberOfOppgaveRecords(5, "dummyTopic")

        val capturedStores = slot<List<Oppgave>>()

        coEvery { persistingService.writeEventsToCache(capture(capturedStores)) } returns emptyPersistResult()

        val slot = slot<suspend EventMetricsSession.() -> Unit>()

        coEvery { metricsProbe.runWithMetrics(any(), capture(slot)) } coAnswers {
            slot.captured.invoke(metricsSession)
        }

        runBlocking {
            eventService.processEvents(records)
        }

        verify(exactly = records.count()) { OppgaveTransformer.toInternal(any(), any()) }
        coVerify(exactly = 1) { persistingService.writeEventsToCache(allAny()) }
        capturedStores.captured.size `should be` records.count()

        confirmVerified(persistingService)
        confirmVerified(OppgaveTransformer)
    }

    @Test
    fun `Should finish processing batch before throwing exception when unable to transform event`() {
        val numberOfRecords = 5
        val numberOfFailedTransformations = 1
        val numberOfSuccessfulTransformations = numberOfRecords - numberOfFailedTransformations

        val records = ConsumerRecordsObjectMother.giveMeANumberOfOppgaveRecords(numberOfRecords, "dummyTopic")
        val transformedRecords = createANumberOfTransformedOppgaveRecords(numberOfSuccessfulTransformations)

        val capturedStores = slot<List<Oppgave>>()

        coEvery { persistingService.writeEventsToCache(capture(capturedStores)) } returns emptyPersistResult()

        val mockedException = UntransformableRecordException("Simulated Exception")

        every { OppgaveTransformer.toInternal(any(), any()) } throws mockedException andThenMany transformedRecords

        val slot = slot<suspend EventMetricsSession.() -> Unit>()

        coEvery { metricsProbe.runWithMetrics(any(), capture(slot)) } coAnswers {
            slot.captured.invoke(metricsSession)
        }

        invoking {
            runBlocking {
                eventService.processEvents(records)
            }
        } `should throw` UntransformableRecordException::class

        verify(exactly = numberOfRecords) { OppgaveTransformer.toInternal(any(), any()) }
        coVerify(exactly = 1) { persistingService.writeEventsToCache(allAny()) }
        coVerify(exactly = numberOfFailedTransformations) { metricsSession.countFailedEventForProducer(any()) }
        capturedStores.captured.size `should be` numberOfSuccessfulTransformations

        confirmVerified(persistingService)
        confirmVerified(OppgaveTransformer)
    }

    @Test
    fun shouldRegisterMetricsForEveryEvent() {
        val numberOfRecords = 5

        val records = ConsumerRecordsObjectMother.giveMeANumberOfOppgaveRecords(numberOfRecords, "oppgave")

        val slot = slot<suspend EventMetricsSession.() -> Unit>()

        coEvery{ persistingService.writeEventsToCache(any()) } returns emptyPersistResult()

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
        val tooLongText = "A".repeat(501)
        val oppgaveWithTooLongText = AvroOppgaveObjectMother.createOppgave(tooLongText)
        val cr = ConsumerRecordsObjectMother.createConsumerRecord("oppgave", oppgaveWithTooLongText)
        val records = ConsumerRecordsObjectMother.giveMeConsumerRecordsWithThisConsumerRecord(cr)

        val slot = slot<suspend EventMetricsSession.() -> Unit>()
        coEvery { metricsProbe.runWithMetrics(any(), capture(slot)) } coAnswers {
            slot.captured.invoke(metricsSession)
        }

        val capturedNumberOfEntitiesWrittenToTheDb = slot<List<Oppgave>>()
        coEvery { persistingService.writeEventsToCache(capture(capturedNumberOfEntitiesWrittenToTheDb)) } returns emptyPersistResult()

        runBlocking {
            eventService.processEvents(records)
        }

        capturedNumberOfEntitiesWrittenToTheDb.captured.size `should be` 0

        coVerify(exactly = 1) { metricsSession.countFailedEventForProducer(any()) }
    }

    @Test
    fun `Skal haandtere at et event med feil type har havnet paa topic`() {
        val numberOfRecords = 1

        val malplacedRecords = ConsumerRecordsObjectMother.giveMeANumberOfBeskjedRecords(numberOfRecords, "oppgave")

        val records = malplacedRecords as ConsumerRecords<Nokkel, no.nav.brukernotifikasjon.schemas.Oppgave>

        val slot = slot<suspend EventMetricsSession.() -> Unit>()

        coEvery{ persistingService.writeEventsToCache(any()) } returns emptyPersistResult()

        coEvery { metricsProbe.runWithMetrics(any(), capture(slot)) } coAnswers {
            slot.captured.invoke(metricsSession)
        }

        val capturedNumberOfEntitiesWrittenToTheDb = slot<List<Oppgave>>()
        coEvery { persistingService.writeEventsToCache(capture(capturedNumberOfEntitiesWrittenToTheDb)) } returns emptyPersistResult()

        runBlocking {
            eventService.processEvents(records)
        }

        capturedNumberOfEntitiesWrittenToTheDb.captured.size `should be` 0

        coVerify (exactly = 1) { metricsSession.countFailedEventForProducer(any()) }
    }


    private fun createANumberOfTransformedOppgaveRecords(number: Int): List<Oppgave> {
        return (1..number).map {
            OppgaveObjectMother.giveMeAktivOppgave(it.toString(), "12345")
        }
    }
}
