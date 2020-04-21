package no.nav.personbruker.dittnav.eventaggregator.beskjed

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import org.amshove.kluent.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.sql.SQLException

class BeskjedQueriesTest {

    private val database = H2Database()

    private val Beskjed1: Beskjed
    private val Beskjed2: Beskjed
    private val Beskjed3: Beskjed
    private val Beskjed4: Beskjed

    private val produsent = "dummyProducer"
    private val fodselsnummer = "12345"
    private val eventId = "2"

    private val allEvents: List<Beskjed>
    private val allEventsForSingleUser: List<Beskjed>

    init {
        Beskjed1 = createBeskjed("1", "12345")
        Beskjed2 = createBeskjed("2", "12345")
        Beskjed3 = createBeskjed("3", "12345")
        Beskjed4 = createBeskjed("4", "6789")
        allEvents = listOf(Beskjed1, Beskjed2, Beskjed3, Beskjed4)
        allEventsForSingleUser = listOf(Beskjed1, Beskjed2, Beskjed3)
    }

    private fun createBeskjed(eventId: String, fodselsnummer: String): Beskjed {
        val beskjed = BeskjedObjectMother.giveMeAktivBeskjed(eventId, fodselsnummer)
        return runBlocking {
            database.dbQuery {
                createBeskjed(beskjed).entityId.let {
                    beskjed.copy(id = it)
                }
            }
        }
    }

    @AfterAll
    fun tearDown() {
        runBlocking {
            database.dbQuery { deleteAllBeskjed() }
        }
    }


    @Test
    fun `Finner alle cachede Beskjed-eventer`() {
        runBlocking {
            val result = database.dbQuery { getAllBeskjed() }
            result.size `should be equal to` allEvents.size
            result `should contain all` allEvents
        }
    }

    @Test
    fun `Finner alle aktive cachede Beskjed-eventer`() {
        runBlocking {
            database.dbQuery { setBeskjedAktivFlag(eventId, produsent, fodselsnummer, false) }
            val result = database.dbQuery { getAllBeskjedByAktiv(true) }
            result `should contain all` listOf(Beskjed1, Beskjed3, Beskjed4)
            result `should not contain` Beskjed2
            database.dbQuery { setBeskjedAktivFlag(eventId, produsent, fodselsnummer, true) }
        }
    }

    @Test
    fun `Finner cachet Beskjed-event med Id`() {
        runBlocking {
            val result = database.dbQuery { Beskjed2.id?.let { getBeskjedById(it) } }
            result `should equal` Beskjed2
        }
    }

    @Test
    fun `Kaster Exception hvis Beskjed-event med Id ikke finnes`() {
        invoking {
            runBlocking {
                database.dbQuery { getBeskjedById(999) }
            }
        } shouldThrow SQLException::class `with message` "Found no rows"
    }

    @Test
    fun `Finner cachede Beskjeds-eventer for fodselsnummer`() {
        runBlocking {
            val result = database.dbQuery { getBeskjedByFodselsnummer(fodselsnummer) }
            result.size `should be equal to` 3
            result `should contain all` allEventsForSingleUser
        }
    }

    @Test
    fun `Returnerer tom liste hvis Beskjeds-eventer for fodselsnummer ikke finnes`() {
        runBlocking {
            val result = database.dbQuery { getBeskjedByFodselsnummer("-1") }
            result.isEmpty() `should be equal to` true
        }
    }

    @Test
    fun `Finner cachet Beskjed-event med eventId`() {
        runBlocking {
            val result = database.dbQuery { getBeskjedByEventId(eventId) }
            result `should equal` Beskjed2
        }
    }

    @Test
    fun `Kaster Exception hvis Beskjed-event med eventId ikke finnes`() {
        invoking {
            runBlocking {
                database.dbQuery { getBeskjedByEventId("-1") }
            }
        } shouldThrow SQLException::class `with message` "Found no rows"
    }

}
