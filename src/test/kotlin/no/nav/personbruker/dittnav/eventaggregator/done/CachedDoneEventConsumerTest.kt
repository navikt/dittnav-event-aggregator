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
    fun `ved prosessering av done-eventer skal det kjores update mot databasen to ganger, en for aktive og en for inaktive`() {
        val beskjed = BrukernotifikasjonObjectMother.giveMeBeskjed()
        val matchingDoneEvent = DoneObjectMother.giveMeMatchingDoneEvent(beskjed)
        val doneEventUtenMatch = DoneObjectMother.giveMeDone("utenMatch")

        coEvery {
            doneRepo.fetchAllDoneEvents()
        } returns listOf(matchingDoneEvent, doneEventUtenMatch)

        coEvery {
            doneRepo.fetchActiveBrukernotifikasjonerFromView()
        } returns listOf(beskjed)

        coEvery {
            doneRepo.fetchInaktiveBrukernotifikasjonerFromView()
        } returns emptyList()

        runBlocking {
            consumer.processDoneEvents()
        }

        coVerify(exactly = 2) { doneRepo.writeDoneEventsForBeskjedToCache(any()) }
        coVerify(exactly = 2) { doneRepo.writeDoneEventsForInnboksToCache(any()) }
        coVerify(exactly = 2) { doneRepo.writeDoneEventsForOppgaveToCache(any()) }
        coVerify(exactly = 2) { doneRepo.deleteDoneEventsFromCache(any()) }
    }
}
