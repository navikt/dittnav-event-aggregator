package no.nav.personbruker.dittnav.eventaggregator.expired

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedObjectMother
import org.amshove.kluent.called
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ExpiredBeskjedProcessorTest {

    private val expiredPersistingService = mockk<ExpiredPersistingService>(relaxed = true)
    private val doneEmitter = mockk<DoneEventEmitter>(relaxed = true)
    private val processor = ExpiredBeskjedProcessor(expiredPersistingService, doneEmitter)

    @BeforeEach
    fun `reset mocks`() {
        clearMocks(expiredPersistingService)
        clearMocks(doneEmitter)
    }

    @Test
    fun `skal sende done-eventer for hver utgaatt beskjed`() {
        val result = listOf(
            BeskjedObjectMother.giveMeAktivBeskjed().copy(id = 1),
            BeskjedObjectMother.giveMeAktivBeskjed().copy(id = 2)
        )
        coEvery { expiredPersistingService.getExpiredBeskjeder()
        } returns result andThen listOf()

        runBlocking {
            processor.sendDoneEventsForExpiredBeskjeder()
        }

        verify(exactly = 1) { doneEmitter.emittBeskjedDone(result) }
    }

    @Test
    fun `Hvis ingen beskjed har utgaatt, ingen done-event skal bli sent`() {
        coEvery { expiredPersistingService.getExpiredBeskjeder() } returns listOf()

        verify(exactly = 0) { doneEmitter.emittBeskjedDone(listOf()) }
    }
}
