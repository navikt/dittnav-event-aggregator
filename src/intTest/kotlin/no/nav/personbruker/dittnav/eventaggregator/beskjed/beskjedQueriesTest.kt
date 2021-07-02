package no.nav.personbruker.dittnav.eventaggregator.beskjed

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.countTotalNumberOfEvents
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.countTotalNumberOfEventsByActiveStatus
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.done.DoneObjectMother
import org.amshove.kluent.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.sql.SQLException

class beskjedQueriesTest {

    private val database = LocalPostgresDatabase()

    private val beskjed1: Beskjed
    private val beskjed2: Beskjed
    private val beskjed3: Beskjed
    private val beskjed4: Beskjed

    private val systembruker = "dummySystembruker"
    private val fodselsnummer = "12345"
    private val eventId = "2"

    private val allEvents: List<Beskjed>
    private val allEventsForSingleUser: List<Beskjed>

    init {
        beskjed1 = createBeskjed("1", "12345")
        beskjed2 = createBeskjed("2", "12345")
        beskjed3 = createBeskjed("3", "12345")
        beskjed4 = createBeskjed("4", "6789")
        allEvents = listOf(beskjed1, beskjed2, beskjed3, beskjed4)
        allEventsForSingleUser = listOf(beskjed1, beskjed2, beskjed3)
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
        val doneEvent = DoneObjectMother.giveMeDone(eventId, systembruker, fodselsnummer)
        runBlocking {
            database.dbQuery { setBeskjederAktivflagg(listOf(doneEvent), false) }
            val result = database.dbQuery { getAllBeskjedByAktiv(true) }
            result `should contain all` listOf(beskjed1, beskjed3, beskjed4)
            result `should not contain` beskjed2
            database.dbQuery { setBeskjederAktivflagg(listOf(doneEvent), true) }
        }
    }

    @Test
    fun `Finner cachet Beskjed-event med Id`() {
        runBlocking {
            val result = database.dbQuery { beskjed2.id?.let { getBeskjedById(it) } }
            result `should be equal to` beskjed2
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
            result `should be equal to` beskjed2
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

    @Test
    fun `Skal haandtere at prefererteKanaler er tom`() {
        val beskjed = BeskjedObjectMother.giveMeAktivBeskjedWithEksternVarslingAndPrefererteKanaler(true, emptyList())
        invoking {
            runBlocking {
                database.dbQuery { createBeskjed(beskjed) }
                val result = database.dbQuery { getBeskjedByEventId(beskjed.eventId) }
                result.prefererteKanaler.`should be empty`()
            }
        }
    }

    @Test
    fun `Skal skrive eventer i batch`() {
        val beskjed1 = BeskjedObjectMother.giveMeAktivBeskjed("b-1", "123")
        val beskjed2 = BeskjedObjectMother.giveMeAktivBeskjed("b-2", "123")

        runBlocking {
            database.dbQuery {
                createBeskjeder(listOf(beskjed1, beskjed2))
            }

            val beskjed1FraDb = database.dbQuery { getBeskjedByEventId(beskjed1.eventId) }
            val beskjed2FraDb = database.dbQuery { getBeskjedByEventId(beskjed2.eventId) }

            beskjed1FraDb.eventId `should be equal to` beskjed1.eventId
            beskjed2FraDb.eventId `should be equal to` beskjed2.eventId

            database.dbQuery { deleteBeskjedWithEventId(beskjed1.eventId) }
            database.dbQuery { deleteBeskjedWithEventId(beskjed2.eventId) }
        }
    }

    @Test
    fun `Skal telle det totale antall beskjeder`() {
        runBlocking {
            database.dbQuery {
                countTotalNumberOfEvents(EventType.BESKJED_INTERN)
            }
        } `should be equal to` allEvents.size.toLong()
    }

    @Test
    fun `Skal telle det totale antall aktive beskjeder`() {
        runBlocking {
            database.dbQuery {
                countTotalNumberOfEventsByActiveStatus(EventType.BESKJED_INTERN, true)
            }
        } `should be equal to` allEvents.size.toLong()
    }

    @Test
    fun `Skal telle det totale antall inaktive beskjeder`() {
        runBlocking {
            database.dbQuery {
                countTotalNumberOfEventsByActiveStatus(EventType.BESKJED_INTERN, false)
            }
        } `should be equal to` 0
    }

}
