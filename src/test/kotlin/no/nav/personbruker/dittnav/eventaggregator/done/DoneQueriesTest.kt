package no.nav.personbruker.dittnav.eventaggregator.done

import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import org.junit.jupiter.api.Test

class DoneQueriesTest {

    private val database = LocalPostgresDatabase.migratedDb()
    private val done1 = DoneObjectMother.giveMeDone("1")
    private val done2 = DoneObjectMother.giveMeDone("2")
    private val done3 = DoneObjectMother.giveMeDone("3")
    private val allEvents = listOf(done1, done2, done3)

    init {
        runBlocking {
            database.dbQuery {
                createDoneEvents(listOf(done1, done2, done3))
            }
        }
    }

    @Test
    fun `Finner alle cachede Done-eventer`() {
        runBlocking {
            val result = database.dbQuery { getAllDoneEvent() }
            result.size shouldBe 3
            result shouldContainAll allEvents
        }
    }

    @Test
    fun `skal slette spesifikke done-eventer`() {
        val doneEvent1 = DoneObjectMother.giveMeDone("111", "dummySystembruker", "123")
        val doneEvent2 = DoneObjectMother.giveMeDone("222", "dummySystembruker", "123")
        val doneEventsToInsertAndThenDelete = listOf(doneEvent1, doneEvent2)

        runBlocking {
            database.dbQuery { createDoneEvents(doneEventsToInsertAndThenDelete) }
            val antallDoneEventerForSletting = database.dbQuery { getAllDoneEvent() }
            val expectedAntallDoneEventerEtterSletting = antallDoneEventerForSletting.size - doneEventsToInsertAndThenDelete.size

            database.dbQuery { deleteDoneEvents(doneEventsToInsertAndThenDelete) }

            val antallDoneEventerEtterSletting = database.dbQuery { getAllDoneEvent() }
            antallDoneEventerEtterSletting.size shouldBe expectedAntallDoneEventerEtterSletting
        }
    }

}
