package no.nav.personbruker.dittnav.eventaggregator.oppgave

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import org.amshove.kluent.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.sql.SQLException

class OppgaveQueriesTest {

    private val database = H2Database()

    private val fodselsnummer1 = "12345"
    private val fodselsnummer2 = "54321"

    private val oppgave1: Oppgave
    private val oppgave2: Oppgave
    private val oppgave3: Oppgave

    private val systembruker = "dummySystembruker"
    private val eventId = "2"

    private val allEvents: List<Oppgave>
    private val allEventsForSingleUser: List<Oppgave>

    init {
        oppgave1 = createOppgave("1", fodselsnummer1)
        oppgave2 = createOppgave("2", fodselsnummer2)
        oppgave3 = createOppgave("3", fodselsnummer1)
        allEvents = listOf(oppgave1, oppgave2, oppgave3)
        allEventsForSingleUser = listOf(oppgave1, oppgave3)
    }

    private fun createOppgave(eventId: String, fodselsnummer: String): Oppgave {
        var oppgave = OppgaveObjectMother.giveMeAktivOppgave(eventId, fodselsnummer)
        runBlocking {
            database.dbQuery {
                val generatedId = createOppgave(oppgave).entityId
                oppgave = oppgave.copy(id=generatedId)
            }
        }
        return oppgave
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
            database.dbQuery { setOppgaveAktivFlag(eventId, systembruker, fodselsnummer2, false) }
            val result = database.dbQuery { getAllOppgaveByAktiv(true) }
            result `should contain all` listOf(oppgave1, oppgave3)
            result `should not contain` oppgave2
            database.dbQuery { setOppgaveAktivFlag(eventId, systembruker, fodselsnummer2,  true) }
        }
    }

    @Test
    fun `Finner alle cachede Oppgave-event for fodselsnummer`() {
        runBlocking {
            val result = database.dbQuery { getOppgaveByFodselsnummer(fodselsnummer1) }
            result.size `should be equal to` allEventsForSingleUser.size
            result `should contain all` allEventsForSingleUser
        }
    }

    @Test
    fun `Gir tom liste dersom Oppgave-event med gitt fodselsnummer ikke finnes`() {
        runBlocking {
            val result = database.dbQuery { getOppgaveByFodselsnummer("-1") }
            result.isEmpty() `should be` true
        }
    }

    @Test
    fun `Finner cachet Oppgave-event for Id`() {
        runBlocking {
            val result = database.dbQuery { oppgave2.id?.let { getOppgaveById(it) } }
            result `should equal` oppgave2
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
            val result = database.dbQuery { getOppgaveByEventId(eventId) }
            result `should equal` oppgave2
        }
    }

    @Test
    fun `Kaster exception dersom Oppgave-event med eventId ikke finnes`() {
        invoking {
            runBlocking {
                database.dbQuery { getOppgaveByEventId("-1") }
            }
        } shouldThrow SQLException::class `with message` "Found no rows"
    }

    @Test
    fun `Persister ikke entitet dersom rad med samme eventId og produsent finnes`() {
        runBlocking {
            database.dbQuery {
                createOppgave(oppgave1)
                val numberOfEvents = getAllOppgave().size
                createOppgave(oppgave1)
                getAllOppgave().size `should be equal to` numberOfEvents
            }
        }
    }
}
