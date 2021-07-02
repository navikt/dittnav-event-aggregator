package no.nav.personbruker.dittnav.eventaggregator.done

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import org.amshove.kluent.`should contain same`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class DoneRepositoryTest {

    val database = LocalPostgresDatabase()

    val doneRepository = DoneRepository(database)

    private val done1 = DoneObjectMother.giveMeDone("11", "12345")
    private val done2 = DoneObjectMother.giveMeDone("12", "12345")
    private val done3 = DoneObjectMother.giveMeDone("13", "12345")

    @AfterEach
    fun tearDown() {
        runBlocking {
            database.dbQuery {
                deleteAllDone()
            }
        }
    }

    @Test
    fun `Should return correct result for successful persists when all events are persisted in a single batch`() {
        runBlocking {
            val toCreate = listOf(done1, done2, done3)
            val result = doneRepository.createInOneBatch(toCreate)
            result.getPersistedEntitites() `should contain same` toCreate
        }
    }

    @Test
    fun `Should return correct result for successful persists when each event is persisted individually`() {
        runBlocking {
            val toCreate = listOf(done1, done2, done3)
            val result = doneRepository.createOneByOneToFilterOutTheProblematicEvents(toCreate)
            result.getPersistedEntitites() `should contain same` toCreate
        }
    }

    @Test
    fun `Should persist done-events in batch and return correct result when some events have unique key constraints`() {
        runBlocking {
            val toCreate = listOf(done1, done2, done3)
            val alreadyPersisted = listOf(done1, done3)
            val expected = toCreate - alreadyPersisted
            doneRepository.createInOneBatch(alreadyPersisted)
            val result = doneRepository.createInOneBatch(toCreate)
            result.getPersistedEntitites() `should contain same` expected
            result.getConflictingEntities() `should contain same` alreadyPersisted
        }
    }

    @Test
    fun `Should persist each done-events individually and return correct result when some events have unique key constraints`() {
        runBlocking {
            val toCreate = listOf(done1, done2, done3)
            val alreadyPersisted = listOf(done1, done3)
            val expected = toCreate - alreadyPersisted
            doneRepository.createInOneBatch(alreadyPersisted)
            val result = doneRepository.createOneByOneToFilterOutTheProblematicEvents(toCreate)
            result.getPersistedEntitites() `should contain same` expected
            result.getConflictingEntities() `should contain same` alreadyPersisted
        }
    }
}
