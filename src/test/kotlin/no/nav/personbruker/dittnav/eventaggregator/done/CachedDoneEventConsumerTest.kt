package no.nav.personbruker.dittnav.eventaggregator.done

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.objectmother.BrukernotifikasjonObjectMother
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsSession
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.DBMetricsSession
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.DBMetricsProbe
import org.junit.jupiter.api.Test

internal class CachedDoneEventConsumerTest {

    private val doneRepo = mockk<DoneRepository>(relaxed = true)
    private val metricsProbe = mockk<DBMetricsProbe>(relaxed = true)
    private val metricsSession = mockk<DBMetricsSession>(relaxed = true)
    private val consumer = CachedDoneEventConsumer(doneRepo, metricsProbe)

    @Test
    fun `ved prosessering av done-eventer skal det kjores update mot databasen kun en gang`() {
        val beskjed = BrukernotifikasjonObjectMother.giveMeBeskjed()
        val matchingDoneEvent = DoneObjectMother.giveMeMatchingDoneEvent(beskjed)
        val doneEventUtenMatch = DoneObjectMother.giveMeDone("utenMatch")

        coEvery {
            doneRepo.fetchAllDoneEvents()
        } returns listOf(matchingDoneEvent, doneEventUtenMatch)

        coEvery {
            doneRepo.fetchBrukernotifikasjonerFromViewForEventIds(any())
        } returns listOf(beskjed)

        runBlocking {
            consumer.processDoneEvents()
        }

        coVerify(exactly = 1) { doneRepo.writeDoneEventsForBeskjedToCache(any()) }
        coVerify(exactly = 1) { doneRepo.writeDoneEventsForInnboksToCache(any()) }
        coVerify(exactly = 1) { doneRepo.writeDoneEventsForOppgaveToCache(any()) }
        coVerify(exactly = 1) { doneRepo.deleteDoneEventsFromCache(any()) }
    }

    @Test
    fun `skal telle og lage metrikk på antall done-eventer vi ikke fant tilhørende oppgave for`() {
        val beskjed = BrukernotifikasjonObjectMother.giveMeBeskjed()
        val doneEvents = listOf(
                DoneObjectMother.giveMeMatchingDoneEvent(beskjed),
                DoneObjectMother.giveMeDone("utenMatch1"),
                DoneObjectMother.giveMeDone("utenMatch2"))

        val slot = slot<suspend DBMetricsSession.() -> Unit>()
        coEvery { metricsProbe.runWithMetrics(any(), capture(slot)) } coAnswers {
            slot.captured.invoke(metricsSession)
        }

        coEvery {
            doneRepo.fetchAllDoneEvents()
        } returns doneEvents

        coEvery {
            doneRepo.fetchBrukernotifikasjonerFromViewForEventIds(any())
        } returns listOf(beskjed)

        runBlocking {
            consumer.processDoneEvents()
        }

        coVerify(exactly = 2) { metricsSession.countCachedEventForProducer("dummySystembruker") }
    }
}
