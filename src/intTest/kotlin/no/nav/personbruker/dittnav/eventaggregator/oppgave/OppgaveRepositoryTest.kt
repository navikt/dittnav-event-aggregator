package no.nav.personbruker.dittnav.eventaggregator.oppgave

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import org.amshove.kluent.`should contain same`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class OppgaveRepositoryTest {

    val database = LocalPostgresDatabase()

    val oppgaveRepository = OppgaveRepository(database)

    private val oppgave1 = OppgaveObjectMother.giveMeAktivOppgave("11", "12345")
    private val oppgave2 = OppgaveObjectMother.giveMeAktivOppgave("12", "12345")
    private val oppgave3 = OppgaveObjectMother.giveMeAktivOppgave("13", "12345")

    @AfterEach
    fun tearDown() {
        runBlocking {
            database.dbQuery {
                deleteAllOppgave()
            }
        }
    }

    @Test
    fun `Should return correct result for successful persists when all events are persisted in a single batch`() {
        runBlocking {
            val toCreate = listOf(oppgave1, oppgave2, oppgave3)

            val result = oppgaveRepository.createInOneBatch(toCreate)

            result.getPersistedEntitites() `should contain same` toCreate
        }
    }

    @Test
    fun `Should return correct result for successful persists when each event is persisted individually`() {
        runBlocking {
            val toCreate = listOf(oppgave1, oppgave2, oppgave3)

            val result = oppgaveRepository.createOneByOneToFilterOutTheProblematicEvents(toCreate)

            result.getPersistedEntitites() `should contain same` toCreate
        }
    }

    @Test
    fun `Should persist oppgaves in batch and return correct result when some events have unique key constraints`() {
        runBlocking {
            val toCreate = listOf(oppgave1, oppgave2, oppgave3)
            val alreadyPersisted = listOf(oppgave1, oppgave3)
            val expected = toCreate - alreadyPersisted

            oppgaveRepository.createInOneBatch(alreadyPersisted)

            val result = oppgaveRepository.createInOneBatch(toCreate)

            result.getPersistedEntitites() `should contain same` expected
            result.getConflictingEntities() `should contain same` alreadyPersisted
        }
    }

    @Test
    fun `Should persist each oppgave individually and return correct result when some events have unique key constraints`() {
        runBlocking {
            val toCreate = listOf(oppgave1, oppgave2, oppgave3)
            val alreadyPersisted = listOf(oppgave1, oppgave3)
            val expected = toCreate - alreadyPersisted

            oppgaveRepository.createInOneBatch(alreadyPersisted)

            val result = oppgaveRepository.createOneByOneToFilterOutTheProblematicEvents(toCreate)

            result.getPersistedEntitites() `should contain same` expected
            result.getConflictingEntities() `should contain same` alreadyPersisted
        }
    }
}