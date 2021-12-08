package no.nav.personbruker.dittnav.eventaggregator.expired

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveObjectMother
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ExpiredNotificationsProcessorTest {

    private val expiredPersistingService = mockk<ExpiredPersistingService>(relaxed = true)
    private val doneEmitter = mockk<DoneEventEmitter>(relaxed = true)
    private val processor = ExpiredNotificationProcessor(expiredPersistingService, doneEmitter)

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
    fun `skal sende done-eventer for hver utgaat oppgave`() {
        val result = listOf(
            OppgaveObjectMother.giveMeAktivOppgave().copy(id = 1),
            OppgaveObjectMother.giveMeAktivOppgave().copy(id = 2)
        )
        coEvery { expiredPersistingService.getExpiredOppgaver()
        } returns result andThen listOf()

        runBlocking {
            processor.sendDoneEventsForExpiredOppgaver()
        }

        verify(exactly = 1) { doneEmitter.emittOppgaveDone(result) }
    }

    @Test
    fun `Hvis ingen beskjed har utgaatt, ingen done-event skal bli sent`() {
        coEvery { expiredPersistingService.getExpiredBeskjeder() } returns listOf()

        runBlocking {
            processor.sendDoneEventsForExpiredBeskjeder()
        }

        verify(exactly = 0) { doneEmitter.emittBeskjedDone(listOf()) }
    }

    @Test
    internal fun `Hvis ingen oppgave har utgaat, ingen done-event skal bli sent`() {
        coEvery { expiredPersistingService.getExpiredOppgaver() } returns listOf()

        runBlocking {
            processor.sendDoneEventsForExpiredOppgaver()
        }

        verify(exactly = 0) { doneEmitter.emittOppgaveDone(listOf()) }
    }
}
