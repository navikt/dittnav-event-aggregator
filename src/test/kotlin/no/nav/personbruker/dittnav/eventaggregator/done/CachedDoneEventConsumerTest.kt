package no.nav.personbruker.dittnav.eventaggregator.done

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.objectmother.BrukernotifikasjonObjectMother
import org.junit.jupiter.api.Test

internal class CachedDoneEventConsumerTest {

    private val doneRepo = mockk<DoneRepository>(relaxed = true)
    private val consumer = CachedDoneEventConsumer(doneRepo)

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
        coVerify(exactly = 1) { doneRepo.deleteDoneEventFromCache(any()) }
    }
}
