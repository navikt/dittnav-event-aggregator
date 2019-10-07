package no.nav.personbruker.dittnav.eventaggregator.database.entity

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.entity.deleteAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.entity.deleteOppgaveWithEventId
import org.amshove.kluent.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.sql.SQLException

class OppgaveQueriesTest {

    private val database = H2Database()

    private val aktorId1 = "12345"
    private val aktorId2 = "54321"

    private val oppgave1 = OppgaveObjectMother.createOppgave(1, aktorId1)
    private val oppgave2 = OppgaveObjectMother.createOppgave(2, aktorId2)
    private val oppgave3 = OppgaveObjectMother.createOppgave(3, aktorId1)

    private val allEvents = listOf(oppgave1, oppgave2, oppgave3)
    private val allEventsForSingleUser = listOf(oppgave1, oppgave3)

    init {
        runBlocking {
            database.dbQuery {
                createOppgave(oppgave1)
                createOppgave(oppgave2)
                createOppgave(oppgave3)
            }
        }
    }

    @AfterAll
    fun tearDown() {
        runBlocking {
            database.dbQuery { deleteAllOppgave() }
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
    fun `Finner alle aktive cachede Oppgave-eventer`() {
        runBlocking {
            val inaktivOppgave = OppgaveObjectMother.createOppgave(5, "12345", false)
            database.dbQuery { createOppgave(inaktivOppgave) }
            val result = database.dbQuery { getAllOppgaveByAktiv(true) }
            result `should contain all` allEvents
            result `should not contain` inaktivOppgave
            database.dbQuery { deleteOppgaveWithEventId(inaktivOppgave.eventId) }
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
    fun `Finner cachet Oppgave-event for Id`() {
        runBlocking {
            val result = database.dbQuery { getOppgaveById(2) }
            result `should not be` oppgave2
        }
    }

    @Test
    fun `Kaster exception dersom Oppgave-event med Id ikke finnes`() {
        invoking {
            runBlocking {
                database.dbQuery { getOppgaveById(-1) }
            }
        } shouldThrow SQLException::class `with message` "Found no rows"
    }

    @Test
    fun `Finner cachet Oppgave-event med eventId`() {
        runBlocking {
            val result = database.dbQuery { getOppgaveByEventId(2) }
            result `should not be` oppgave2
        }
    }

    @Test
    fun `Kaster exception dersom Oppgave-event med eventId ikke finnes`() {
        invoking {
            runBlocking {
                database.dbQuery { getOppgaveByEventId(-1) }
            }
        } shouldThrow SQLException::class `with message` "Found no rows"
    }
}
