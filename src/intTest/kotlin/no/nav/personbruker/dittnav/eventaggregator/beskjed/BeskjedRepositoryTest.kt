package no.nav.personbruker.dittnav.eventaggregator.beskjed

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import org.amshove.kluent.`should contain same`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class BeskjedRepositoryTest {

    val database = LocalPostgresDatabase.migratedDb()

    val beskjedRepository = BeskjedRepository(database)

    private val beskjed1 = BeskjedObjectMother.giveMeAktivBeskjed("11", "12345")
    private val beskjed2 = BeskjedObjectMother.giveMeAktivBeskjed("12", "12345")
    private val beskjed3 = BeskjedObjectMother.giveMeAktivBeskjed("13", "12345")

    @AfterEach
    fun tearDown() {
        runBlocking {
            database.dbQuery {
                deleteAllBeskjed()
            }
        }
    }

    @Test
    fun `Should return correct result for successful persists when all events are persisted in a single batch`() {
        runBlocking {
            val toCreate = listOf(beskjed1, beskjed2, beskjed3)

            val result = beskjedRepository.createInOneBatch(toCreate)

            result.getPersistedEntitites() `should contain same` toCreate
        }
    }

    @Test
    fun `Should return correct result for successful persists when each event is persisted individually`() {
        runBlocking {
            val toCreate = listOf(beskjed1, beskjed2, beskjed3)

            val result = beskjedRepository.createOneByOneToFilterOutTheProblematicEvents(toCreate)

            result.getPersistedEntitites() `should contain same` toCreate
        }
    }

    @Test
    fun `Should persist beskjeds in batch and return correct result when some events have unique key constraints`() {
        runBlocking {
            val toCreate = listOf(beskjed1, beskjed2, beskjed3)
            val alreadyPersisted = listOf(beskjed1, beskjed3)
            val expected = toCreate - alreadyPersisted

            beskjedRepository.createInOneBatch(alreadyPersisted)

            val result = beskjedRepository.createInOneBatch(toCreate)

            result.getPersistedEntitites() `should contain same` expected
            result.getConflictingEntities() `should contain same` alreadyPersisted
        }
    }

    @Test
    fun `Should persist each beskjed individually and return correct result when some events have unique key constraints`() {
        runBlocking {
            val toCreate = listOf(beskjed1, beskjed2, beskjed3)
            val alreadyPersisted = listOf(beskjed1, beskjed3)
            val expected = toCreate - alreadyPersisted

            beskjedRepository.createInOneBatch(alreadyPersisted)

            val result = beskjedRepository.createOneByOneToFilterOutTheProblematicEvents(toCreate)

            result.getPersistedEntitites() `should contain same` expected
            result.getConflictingEntities() `should contain same` alreadyPersisted
        }
    }
}