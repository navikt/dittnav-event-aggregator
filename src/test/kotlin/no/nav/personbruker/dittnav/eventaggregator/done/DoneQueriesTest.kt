package no.nav.personbruker.dittnav.eventaggregator.done

import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import org.junit.jupiter.api.Test

class DoneQueriesTest {

    private val database = LocalPostgresDatabase.migratedDb()
    private val done1 = DoneTestData.done("1")
    private val done2 = DoneTestData.done("2")
    private val done3 = DoneTestData.done("3")
    private val allEvents = listOf(done1, done2, done3)

    init {
        runBlocking {
            database.dbQuery {
                listOf(done1, done2, done3).forEach { createDoneEvent(it) }
            }
        }
    }

    @Test
    fun `Finner alle cachede Done-eventer`() {
        runBlocking {
            val result = database.dbQuery { getAllDoneEventWithLimit(100) }
            result.size shouldBe 3
            result shouldContainAll allEvents
        }
    }

    @Test
    fun `skal slette spesifikke done-eventer`() {
        val doneEvent1 = DoneTestData.done("111", "dummySystembruker", "123")
        val doneEvent2 = DoneTestData.done("222", "dummySystembruker", "123")
        val doneEventsToInsertAndThenDelete = listOf(doneEvent1, doneEvent2)

        runBlocking {
            database.dbQuery { doneEventsToInsertAndThenDelete.forEach { createDoneEvent(it) } }
            val antallDoneEventerForSletting = database.dbQuery { getAllDoneEventWithLimit(100) }
            val expectedAntallDoneEventerEtterSletting = antallDoneEventerForSletting.size - doneEventsToInsertAndThenDelete.size

            database.dbQuery { deleteDoneEvents(doneEventsToInsertAndThenDelete) }

            val antallDoneEventerEtterSletting = database.dbQuery { getAllDoneEventWithLimit(100) }
            antallDoneEventerEtterSletting.size shouldBe expectedAntallDoneEventerEtterSletting
        }
    }

}
