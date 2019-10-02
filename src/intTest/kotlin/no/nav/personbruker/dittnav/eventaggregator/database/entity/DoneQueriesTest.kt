package no.nav.personbruker.dittnav.eventaggregator.database.entity

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.entity.deleteAllDone
import no.nav.personbruker.dittnav.eventaggregator.entity.objectmother.DoneObjectMother
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain all`
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class DoneQueriesTest {

    val database = H2Database()
    val done1 = DoneObjectMother.createDone("1")
    val done2 = DoneObjectMother.createDone("2")
    val done3 = DoneObjectMother.createDone("3")
    val allEvents = listOf(done1, done2, done3)

    init {
        runBlocking {
            database.dbQuery {
                createDone(done1)
                createDone(done2)
                createDone(done3)
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
            val result = database.dbQuery { getAllDone() }
            result.size `should be equal to` 3
            result `should contain all` allEvents
        }
    }
}
