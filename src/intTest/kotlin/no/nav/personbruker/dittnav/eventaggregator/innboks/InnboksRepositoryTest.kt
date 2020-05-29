package no.nav.personbruker.dittnav.eventaggregator.innboks

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import org.amshove.kluent.`should contain same`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class InnboksRepositoryTest {

    val database = H2Database()

    val innboksRepository = InnboksRepository(database)

    private val innboks1 = InnboksObjectMother.giveMeAktivInnboks("1", "12345")
    private val innboks2 = InnboksObjectMother.giveMeAktivInnboks("2", "12345")
    private val innboks3 = InnboksObjectMother.giveMeAktivInnboks("3", "12345")

    @AfterEach
    fun tearDown() {
        runBlocking {
            database.dbQuery {
                deleteAllInnboks()
            }
        }
    }

    @Test
    fun `Should return correct result for successful persists when all events are persisted in a single batch`() {
        runBlocking {
            val toCreate = listOf(innboks1, innboks2, innboks3)

            val result = innboksRepository.createInOneBatch(toCreate)

            result.getPersistedEntitites() `should contain same` toCreate
        }
    }

    @Test
    fun `Should return correct result for successful persists when each event is persisted individually`() {
        runBlocking {
            val toCreate = listOf(innboks1, innboks2, innboks3)

            val result = innboksRepository.createOneByOneToFilterOutTheProblematicEvents(toCreate)

            result.getPersistedEntitites() `should contain same` toCreate
        }
    }

    @Test
    fun `Should persist innbokss in batch and return correct result when some events have unique key constraints`() {
        runBlocking {
            val toCreate = listOf(innboks1, innboks2, innboks3)
            val alreadyPersisted = listOf(innboks1, innboks3)
            val expected = toCreate - alreadyPersisted

            innboksRepository.createInOneBatch(alreadyPersisted)

            val result = innboksRepository.createInOneBatch(toCreate)

            result.getPersistedEntitites() `should contain same` expected
            result.getConflictingEntities() `should contain same` alreadyPersisted
        }
    }

    @Test
    fun `Should persist each innboks individually and return correct result when some events have unique key constraints`() {
        runBlocking {
            val toCreate = listOf(innboks1, innboks2, innboks3)
            val alreadyPersisted = listOf(innboks1, innboks3)
            val expected = toCreate - alreadyPersisted

            innboksRepository.createInOneBatch(alreadyPersisted)

            val result = innboksRepository.createOneByOneToFilterOutTheProblematicEvents(toCreate)

            result.getPersistedEntitites() `should contain same` expected
            result.getConflictingEntities() `should contain same` alreadyPersisted
        }
    }
}