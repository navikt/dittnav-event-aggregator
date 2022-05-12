package no.nav.personbruker.dittnav.eventaggregator.innboks

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class InnboksRepositoryTest {

    val database = LocalPostgresDatabase.migratedDb()

    val innboksRepository = InnboksRepository(database)

    private val innboks1 = InnboksObjectMother.giveMeAktivInnboks("11", "12345")
    private val innboks2 = InnboksObjectMother.giveMeAktivInnboks("12", "12345")
    private val innboks3 = InnboksObjectMother.giveMeAktivInnboks("13", "12345")

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

            result.getPersistedEntitites() shouldBe toCreate
        }
    }

    @Test
    fun `Should return correct result for successful persists when each event is persisted individually`() {
        runBlocking {
            val toCreate = listOf(innboks1, innboks2, innboks3)

            val result = innboksRepository.createOneByOneToFilterOutTheProblematicEvents(toCreate)

            result.getPersistedEntitites() shouldBe toCreate
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

            result.getPersistedEntitites() shouldBe expected
            result.getConflictingEntities() shouldBe alreadyPersisted
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

            result.getPersistedEntitites() shouldBe expected
            result.getConflictingEntities() shouldBe alreadyPersisted
        }
    }
}