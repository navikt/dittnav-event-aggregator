package no.nav.personbruker.dittnav.eventaggregator.statusOppdatering

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import org.amshove.kluent.`should contain same`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class StatusOppdateringRepositoryTest {

    val database = H2Database()

    val statusOppdateringRepository = StatusOppdateringRepository(database)

    private val statusOppdatering1 = StatusOppdateringObjectMother.giveMeStatusOppdatering("11", "12345")
    private val statusOppdatering2 = StatusOppdateringObjectMother.giveMeStatusOppdatering("12", "12345")
    private val statusOppdatering3 = StatusOppdateringObjectMother.giveMeStatusOppdatering("13", "12345")

    @AfterEach
    fun tearDown() {
        runBlocking {
            database.dbQuery {
                deleteAllStatusOppdatering()
            }
        }
    }

    @Test
    fun `Should return correct result for successful persists when all events are persisted in a single batch`() {
        runBlocking {
            val toCreate = listOf(statusOppdatering1, statusOppdatering2, statusOppdatering3)

            val result = statusOppdateringRepository.createInOneBatch(toCreate)

            result.getPersistedEntitites() `should contain same` toCreate
        }
    }

    @Test
    fun `Should return correct result for successful persists when each event is persisted individually`() {
        runBlocking {
            val toCreate = listOf(statusOppdatering1, statusOppdatering2, statusOppdatering3)

            val result = statusOppdateringRepository.createOneByOneToFilterOutTheProblematicEvents(toCreate)

            result.getPersistedEntitites() `should contain same` toCreate
        }
    }

    @Test
    fun `Should persist statusOppdatering in batch and return correct result when some events have unique key constraints`() {
        runBlocking {
            val toCreate = listOf(statusOppdatering1, statusOppdatering2, statusOppdatering3)
            val alreadyPersisted = listOf(statusOppdatering1, statusOppdatering3)
            val expected = toCreate - alreadyPersisted

            statusOppdateringRepository.createInOneBatch(alreadyPersisted)

            val result = statusOppdateringRepository.createInOneBatch(toCreate)

            result.getPersistedEntitites() `should contain same` expected
            result.getConflictingEntities() `should contain same` alreadyPersisted
        }
    }

    @Test
    fun `Should persist each statusOppdatering individually and return correct result when some events have unique key constraints`() {
        runBlocking {
            val toCreate = listOf(statusOppdatering1, statusOppdatering2, statusOppdatering3)
            val alreadyPersisted = listOf(statusOppdatering1, statusOppdatering3)
            val expected = toCreate - alreadyPersisted

            statusOppdateringRepository.createInOneBatch(alreadyPersisted)

            val result = statusOppdateringRepository.createOneByOneToFilterOutTheProblematicEvents(toCreate)

            result.getPersistedEntitites() `should contain same` expected
            result.getConflictingEntities() `should contain same` alreadyPersisted
        }
    }
}