package no.nav.personbruker.dittnav.eventaggregator.done

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.emptyPersistResult
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.common.objectmother.BrukernotifikasjonObjectMother
import no.nav.personbruker.dittnav.eventaggregator.common.objectmother.ConsumerRecordsObjectMother
import no.nav.personbruker.dittnav.eventaggregator.common.objectmother.ConsumerRecordsObjectMother.createMatchingRecords
import no.nav.personbruker.dittnav.eventaggregator.done.schema.AvroDoneObjectMother
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsSession
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.invoking
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DoneEventServiceTest {

    private val persistingService = mockk<DonePersistingService>(relaxed = true)
    private val metricsProbe = mockk<EventMetricsProbe>(relaxed = true)
    private val metricsSession = mockk<EventMetricsSession>(relaxed = true)
    private val service = DoneEventService(persistingService, metricsProbe)

    private val dummyFnr = "1".repeat(11)

    @BeforeEach
    private fun `reset mocks`() {
        mockkObject(DoneTransformer)
        clearMocks(persistingService)
        clearMocks(metricsProbe)
        clearMocks(metricsSession)
        coEvery { persistingService.writeEventsToCache(any()) } returns ListPersistActionResult.emptyInstance()
        `mock slik at innholdet i runWithMetrics alltid kjores`()
    }

    private fun `mock slik at innholdet i runWithMetrics alltid kjores`() {
        val slot = slot<suspend EventMetricsSession.() -> Unit>()
        coEvery { metricsProbe.runWithMetrics(any(), capture(slot)) } coAnswers {
            slot.captured.invoke(metricsSession)
        }
    }

    @AfterAll
    private fun `clean up`() {
        unmockkAll()
    }

    @Test
    fun `skal oppdatere beskjed-eventer som det blir funnet match for`() {
        val beskjedInDbToMatch = BrukernotifikasjonObjectMother.giveMeBeskjed(dummyFnr)
        val records = createMatchingRecords(beskjedInDbToMatch)

        val capturedNumberOfBeskjedEntitiesWrittenToTheDb = slot<List<Done>>()
        coEvery { persistingService.writeDoneEventsForBeskjedToCache(capture(capturedNumberOfBeskjedEntitiesWrittenToTheDb)) } returns Unit

        coEvery {
            persistingService.fetchBrukernotifikasjonerFromViewForEventIds(any())
        } returns listOf(beskjedInDbToMatch)

        runBlocking {
            service.processEvents(records)
        }

        capturedNumberOfBeskjedEntitiesWrittenToTheDb.captured.size `should be` 1

        coVerify(exactly = 1) { persistingService.writeDoneEventsForBeskjedToCache(any()) }
        coVerify(exactly = 1) { persistingService.writeDoneEventsForInnboksToCache(emptyList()) }
        coVerify(exactly = 1) { persistingService.writeDoneEventsForOppgaveToCache(emptyList()) }
        coVerify(exactly = 1) { persistingService.writeEventsToCache(emptyList()) }
    }

    @Test
    fun `skal oppdatere innboks-eventer som det blir funnet match for`() {
        val innboksEventInDbToMatch = BrukernotifikasjonObjectMother.giveMeInnboks(dummyFnr)
        val records = createMatchingRecords(innboksEventInDbToMatch)

        val capturedNumberOfInnboksEntitiesWrittenToTheDb = slot<List<Done>>()
        coEvery { persistingService.writeDoneEventsForInnboksToCache(capture(capturedNumberOfInnboksEntitiesWrittenToTheDb)) } returns Unit

        coEvery {
            persistingService.fetchBrukernotifikasjonerFromViewForEventIds(any())
        } returns listOf(innboksEventInDbToMatch)

        runBlocking {
            service.processEvents(records)
        }

        capturedNumberOfInnboksEntitiesWrittenToTheDb.captured.size `should be` 1

        coVerify(exactly = 1) { persistingService.writeDoneEventsForInnboksToCache(any()) }
        coVerify(exactly = 1) { persistingService.writeDoneEventsForBeskjedToCache(emptyList()) }
        coVerify(exactly = 1) { persistingService.writeDoneEventsForOppgaveToCache(emptyList()) }
        coVerify(exactly = 1) { persistingService.writeEventsToCache(emptyList()) }
    }

    @Test
    fun `skal oppdatere oppgave-eventer som det blir funnet match for`() {
        val oppgaveEventInDbToMatch = BrukernotifikasjonObjectMother.giveMeOppgave(dummyFnr)
        val records = createMatchingRecords(oppgaveEventInDbToMatch)

        val capturedNumberOfOppgaveEntitiesWrittenToTheDb = slot<List<Done>>()
        coEvery { persistingService.writeDoneEventsForOppgaveToCache(capture(capturedNumberOfOppgaveEntitiesWrittenToTheDb)) } returns Unit

        coEvery {
            persistingService.fetchBrukernotifikasjonerFromViewForEventIds(any())
        } returns listOf(oppgaveEventInDbToMatch)

        runBlocking {
            service.processEvents(records)
        }

        capturedNumberOfOppgaveEntitiesWrittenToTheDb.captured.size `should be` 1

        coVerify(exactly = 1) { persistingService.writeDoneEventsForOppgaveToCache(any()) }
        coVerify(exactly = 1) { persistingService.writeDoneEventsForInnboksToCache(emptyList()) }
        coVerify(exactly = 1) { persistingService.writeDoneEventsForBeskjedToCache(emptyList()) }
        coVerify(exactly = 1) { persistingService.writeEventsToCache(emptyList()) }
    }

    @Test
    fun `skal ignorere event med ugyldig nokkel`() {
        val beskjedEventInDbToMatch = BrukernotifikasjonObjectMother.giveMeBeskjed(dummyFnr)
        val matchingDoneEvent = AvroDoneObjectMother.createDone(beskjedEventInDbToMatch.eventId)
        val doneEventWithoutKey = AvroDoneObjectMother.createDoneRecord(null, matchingDoneEvent)
        val records = ConsumerRecordsObjectMother.giveMeConsumerRecordsWithThisConsumerRecord(doneEventWithoutKey)

        coEvery {
            persistingService.fetchBrukernotifikasjonerFromViewForEventIds(any())
        } returns listOf(beskjedEventInDbToMatch)

        runBlocking {
            service.processEvents(records)
        }

        coVerify(exactly = 1) { persistingService.writeDoneEventsForOppgaveToCache(emptyList()) }
        coVerify(exactly = 1) { persistingService.writeDoneEventsForInnboksToCache(emptyList()) }
        coVerify(exactly = 1) { persistingService.writeDoneEventsForBeskjedToCache(emptyList()) }
        coVerify(exactly = 1) { persistingService.writeEventsToCache(emptyList()) }
    }

    @Test
    fun `skal skrive done-eventer det ikke blir funnet match for inn i ventetabellen`() {
        val beskjedInDbToMatch = BrukernotifikasjonObjectMother.giveMeBeskjed(dummyFnr)
        val doneEvent = AvroDoneObjectMother.createDoneRecord("eventIdUteMatch", beskjedInDbToMatch.fodselsnummer)
        val records = ConsumerRecordsObjectMother.giveMeConsumerRecordsWithThisConsumerRecord(doneEvent)

        val capturedNumberOfDoneWrittenToTheDb = slot<List<Done>>()
        coEvery { persistingService.writeEventsToCache(capture(capturedNumberOfDoneWrittenToTheDb)) } returns emptyPersistResult()

        coEvery {
            persistingService.fetchBrukernotifikasjonerFromViewForEventIds(any())
        } returns listOf(beskjedInDbToMatch)

        runBlocking {
            service.processEvents(records)
        }

        capturedNumberOfDoneWrittenToTheDb.captured.size `should be` 1

        coVerify(exactly = 1) { persistingService.writeDoneEventsForBeskjedToCache(emptyList()) }
        coVerify(exactly = 1) { persistingService.writeDoneEventsForInnboksToCache(emptyList()) }
        coVerify(exactly = 1) { persistingService.writeDoneEventsForOppgaveToCache(emptyList()) }
        coVerify(exactly = 1) { persistingService.writeEventsToCache(any()) }
    }

    @Test
    fun `skal kaste exception hvis det skjer minst en ukjent feil`() {
        val beskjedInDbToMatch1 = BrukernotifikasjonObjectMother.giveMeBeskjed(dummyFnr)
        val beskjedInDbToMatch2 = BrukernotifikasjonObjectMother.giveMeBeskjed(dummyFnr)
        val beskjedInDbToMatch3 = BrukernotifikasjonObjectMother.giveMeBeskjed(dummyFnr)
        val entitiesInDbToMatch = listOf(beskjedInDbToMatch1, beskjedInDbToMatch2, beskjedInDbToMatch3)
        val records = createMatchingRecords(entitiesInDbToMatch)

        val capturedNumberOfBeskjedEntitiesWrittenToTheDb = slot<List<Done>>()
        coEvery { persistingService.writeDoneEventsForBeskjedToCache(capture(capturedNumberOfBeskjedEntitiesWrittenToTheDb)) } returns Unit

        val simulertFeil = UntransformableRecordException("Simulert feil")
        val matchingDoneEvent2 = DoneObjectMother.giveMeMatchingDoneEvent(beskjedInDbToMatch2)
        val matchingDoneEvent3 = DoneObjectMother.giveMeMatchingDoneEvent(beskjedInDbToMatch3)
        val matchingDoneEvents = listOf(matchingDoneEvent2, matchingDoneEvent3)
        every {
            DoneTransformer.toInternal(any(), any())
        } throws simulertFeil andThenMany matchingDoneEvents

        coEvery {
            persistingService.fetchBrukernotifikasjonerFromViewForEventIds(any())
        } returns listOf(beskjedInDbToMatch2, beskjedInDbToMatch3)

        invoking {
            runBlocking {
                service.processEvents(records)
            }
        } `should throw` UntransformableRecordException::class

        capturedNumberOfBeskjedEntitiesWrittenToTheDb.captured.size `should be` 2

        coVerify(exactly = 1) { persistingService.writeDoneEventsForBeskjedToCache(any()) }
        coVerify(exactly = 1) { persistingService.writeDoneEventsForInnboksToCache(emptyList()) }
        coVerify(exactly = 1) { persistingService.writeDoneEventsForOppgaveToCache(emptyList()) }
        coVerify(exactly = 1) { persistingService.writeEventsToCache(emptyList()) }
    }

    @Test
    fun `skal forkaste eventer som har valideringsfeil`() {
        val tooLongFodselsnr = "1".repeat(12)
        val beskjedInDbToMatch = BrukernotifikasjonObjectMother.giveMeBeskjed(tooLongFodselsnr)
        val records = createMatchingRecords(beskjedInDbToMatch)

        coEvery {
            persistingService.fetchBrukernotifikasjonerFromViewForEventIds(any())
        } returns listOf(beskjedInDbToMatch)

        runBlocking {
            service.processEvents(records)
        }

        coVerify(exactly = 1) { persistingService.writeDoneEventsForBeskjedToCache(emptyList()) }
        coVerify(exactly = 1) { persistingService.writeDoneEventsForInnboksToCache(emptyList()) }
        coVerify(exactly = 1) { persistingService.writeDoneEventsForOppgaveToCache(emptyList()) }
        coVerify(exactly = 1) { persistingService.writeEventsToCache(emptyList()) }
    }

    @Test
    fun `skal haandtere at et event med feil type har havnet paa topic`() {
        val beskjed = ConsumerRecordsObjectMother.giveMeANumberOfBeskjedRecords(1, "done")

        val records = beskjed as ConsumerRecords<Nokkel, no.nav.brukernotifikasjon.schemas.Done>

        coEvery {
            persistingService.fetchBrukernotifikasjonerFromViewForEventIds(any())
        } returns emptyList()

        runBlocking {
            service.processEvents(records)
        }

        coVerify(exactly = 1) { persistingService.writeDoneEventsForBeskjedToCache(emptyList()) }
        coVerify(exactly = 1) { persistingService.writeDoneEventsForInnboksToCache(emptyList()) }
        coVerify(exactly = 1) { persistingService.writeDoneEventsForOppgaveToCache(emptyList()) }
        coVerify(exactly = 1) { persistingService.writeEventsToCache(emptyList()) }
    }
}
