package no.nav.personbruker.dittnav.eventaggregator.done

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain all`
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class DoneQueriesTest {

    private val database = H2Database()
    private val done1 = DoneObjectMother.createDone("1")
    private val done2 = DoneObjectMother.createDone("2")
    private val done3 = DoneObjectMother.createDone("3")
    private val allEvents = listOf(done1, done2, done3)

    init {
        runBlocking {
            database.dbQuery {
                createDoneEvent(done1)
                createDoneEvent(done2)
                createDoneEvent(done3)
            }
        }
    }

    @AfterAll
    fun tearDown() {
        runBlocking {
            database.dbQuery { deleteAllDone() }
        }
    }

    @Test
    fun `Finner alle cachede Done-eventer`() {
        runBlocking {
            val result = database.dbQuery { getAllDoneEvent() }
            result.size `should be equal to` 3
            result `should contain all` allEvents
        }
    }
}
