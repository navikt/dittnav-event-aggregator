package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import org.amshove.kluent.`should contain same`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class StatusoppdateringRepositoryTest {

    val database = LocalPostgresDatabase()

    val statusoppdateringRepository = StatusoppdateringRepository(database)

    private val statusoppdatering1 = StatusoppdateringObjectMother.giveMeStatusoppdatering("11", "12345")
    private val statusoppdatering2 = StatusoppdateringObjectMother.giveMeStatusoppdatering("12", "12345")
    private val statusoppdatering3 = StatusoppdateringObjectMother.giveMeStatusoppdatering("13", "12345")

    @AfterEach
    fun tearDown() {
        runBlocking {
            database.dbQuery {
                deleteAllStatusoppdatering()
            }
        }
    }

    @Test
    fun `Should return correct result for successful persists when all events are persisted in a single batch`() {
        runBlocking {
            val toCreate = listOf(statusoppdatering1, statusoppdatering2, statusoppdatering3)

            val result = statusoppdateringRepository.createInOneBatch(toCreate)

            result.getPersistedEntitites() `should contain same` toCreate
        }
    }

    @Test
    fun `Should return correct result for successful persists when each event is persisted individually`() {
        runBlocking {
            val toCreate = listOf(statusoppdatering1, statusoppdatering2, statusoppdatering3)

            val result = statusoppdateringRepository.createOneByOneToFilterOutTheProblematicEvents(toCreate)

            result.getPersistedEntitites() `should contain same` toCreate
        }
    }

    @Test
    fun `Should persist statusoppdatering in batch and return correct result when some events have unique key constraints`() {
        runBlocking {
            val toCreate = listOf(statusoppdatering1, statusoppdatering2, statusoppdatering3)
            val alreadyPersisted = listOf(statusoppdatering1, statusoppdatering3)
            val expected = toCreate - alreadyPersisted

            statusoppdateringRepository.createInOneBatch(alreadyPersisted)

            val result = statusoppdateringRepository.createInOneBatch(toCreate)

            result.getPersistedEntitites() `should contain same` expected
            result.getConflictingEntities() `should contain same` alreadyPersisted
        }
    }

    @Test
    fun `Should persist each statusoppdatering individually and return correct result when some events have unique key constraints`() {
        runBlocking {
            val toCreate = listOf(statusoppdatering1, statusoppdatering2, statusoppdatering3)
            val alreadyPersisted = listOf(statusoppdatering1, statusoppdatering3)
            val expected = toCreate - alreadyPersisted

            statusoppdateringRepository.createInOneBatch(alreadyPersisted)

            val result = statusoppdateringRepository.createOneByOneToFilterOutTheProblematicEvents(toCreate)

            result.getPersistedEntitites() `should contain same` expected
            result.getConflictingEntities() `should contain same` alreadyPersisted
        }
    }
}