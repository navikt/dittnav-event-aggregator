package no.nav.personbruker.dittnav.eventaggregator.database.entity

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.database.H2Database
import org.amshove.kluent.*
import org.junit.jupiter.api.Test
import java.sql.SQLException

class OppgaveQueriesTest {
    val database = H2Database()

    val aktorId1 = "12345"
    val aktorId2 = "54321"

    val oppgave1 = OppgaveObjectMother.createOppgave(1, aktorId1)
    val oppgave2 = OppgaveObjectMother.createOppgave(2, aktorId2)
    val oppgave3 = OppgaveObjectMother.createOppgave(3, aktorId1)

    val allEvents = listOf(oppgave1, oppgave2, oppgave3)
    val allEventsForSingleUser = listOf(oppgave1, oppgave3)

    init {
        runBlocking {
            database.dbQuery {
                createOppgave(oppgave1)
                createOppgave(oppgave2)
                createOppgave(oppgave3)
            }
        }
    }

    @Test
    fun `Finner alle cachede Oppgave-eventer`() {
        runBlocking {
            val result = database.dbQuery { getAllOppgave() }

            result.size `should be equal to` allEvents.size
            result `should contain all` allEvents
        }
    }

    @Test
    fun `Finner alle cachede Oppgave-event for aktorId`() {
        runBlocking {
            val result = database.dbQuery { getOppgaveByAktorId(aktorId1) }
            result.size `should be equal to` allEventsForSingleUser.size
            result `should contain all` allEventsForSingleUser
        }
    }

    @Test
    fun `Gir tom liste dersom Oppgave-event med gitt aktorId ikke finnes`() {
        runBlocking {
            val result = database.dbQuery { getOppgaveByAktorId("-1") }
            result.isEmpty() `should be` true
        }
    }

    @Test
    fun `Finner cachet Oppgave-event for id`() {
        runBlocking {
            val result = database.dbQuery { getOppgaveById(2) }
            result `should not be` oppgave2
        }
    }

    @Test
    fun `Kaster exception dersom Oppgave-event med id ikke finnes`() {
        invoking {
            runBlocking {
                database.dbQuery { getOppgaveById(-1) }
            }
        } shouldThrow SQLException::class `with message` "Found no rows"
    }
}