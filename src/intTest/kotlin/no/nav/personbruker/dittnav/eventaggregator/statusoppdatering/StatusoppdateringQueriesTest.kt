package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.countTotalNumberOfEvents
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import org.amshove.kluent.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.sql.SQLException

class StatusoppdateringQueriesTest {

    private val database = LocalPostgresDatabase.migratedDb()

    private val statusoppdatering1: Statusoppdatering
    private val statusoppdatering2: Statusoppdatering
    private val statusoppdatering3: Statusoppdatering
    private val statusoppdatering4: Statusoppdatering

    private val systembruker = "dummySystembruker"
    private val fodselsnummer = "12345"
    private val eventId = "2"

    private val allEvents: List<Statusoppdatering>
    private val allEventsForSingleUser: List<Statusoppdatering>

    init {
        statusoppdatering1 = createStatusoppdatering("1", "12345")
        statusoppdatering2 = createStatusoppdatering("2", "12345")
        statusoppdatering3 = createStatusoppdatering("3", "12345")
        statusoppdatering4 = createStatusoppdatering("4", "6789")
        allEvents = listOf(statusoppdatering1, statusoppdatering2, statusoppdatering3, statusoppdatering4)
        allEventsForSingleUser = listOf(statusoppdatering1, statusoppdatering2, statusoppdatering3)
    }

    private fun createStatusoppdatering(eventId: String, fodselsnummer: String): Statusoppdatering {
        val statusoppdatering = StatusoppdateringObjectMother.giveMeStatusoppdatering(eventId, fodselsnummer)
        return runBlocking {
            database.dbQuery {
                createStatusoppdatering(statusoppdatering).entityId.let {
                    statusoppdatering.copy(id = it)
                }
            }
        }
    }

    @AfterAll
    fun tearDown() {
        runBlocking {
            database.dbQuery { deleteAllStatusoppdatering() }
        }
    }

    @Test
    fun `Finner alle cachede Statusoppdatering-eventer`() {
        runBlocking {
            val result = database.dbQuery { getAllStatusoppdatering() }
            result.size `should be equal to` allEvents.size
            result `should contain all` allEvents
        }
    }

    @Test
    fun `Finner cachet Statusoppdatering-event med Id`() {
        runBlocking {
            val result = database.dbQuery { statusoppdatering2.id?.let { getStatusoppdateringById(it) } }
            result `should be equal to` statusoppdatering2
        }
    }

    @Test
    fun `Kaster Exception hvis Statusoppdatering-event med Id ikke finnes`() {
        invoking {
            runBlocking {
                database.dbQuery { getStatusoppdateringById(999) }
            }
        } shouldThrow SQLException::class `with message` "Found no rows"
    }

    @Test
    fun `Finner cachede Statusoppdaterings-eventer for fodselsnummer`() {
        runBlocking {
            val result = database.dbQuery { getStatusoppdateringByFodselsnummer(fodselsnummer) }
            result.size `should be equal to` 3
            result `should contain all` allEventsForSingleUser
        }
    }

    @Test
    fun `Returnerer tom liste hvis Statusoppdaterings-eventer for fodselsnummer ikke finnes`() {
        runBlocking {
            val result = database.dbQuery { getStatusoppdateringByFodselsnummer("-1") }
            result.isEmpty() `should be equal to` true
        }
    }

    @Test
    fun `Finner cachet Statusoppdatering-event med eventId`() {
        runBlocking {
            val result = database.dbQuery { getStatusoppdateringByEventId(eventId) }
            result `should be equal to` statusoppdatering2
        }
    }

    @Test
    fun `Kaster Exception hvis Statusoppdatering-event med eventId ikke finnes`() {
        invoking {
            runBlocking {
                database.dbQuery { getStatusoppdateringByEventId("-1") }
            }
        } shouldThrow SQLException::class `with message` "Found no rows"
    }

    @Test
    fun `Skal skrive eventer i batch`() {
        val statusoppdatering1 = StatusoppdateringObjectMother.giveMeStatusoppdatering("b-1", "123")
        val statusoppdatering2 = StatusoppdateringObjectMother.giveMeStatusoppdatering("b-2", "123")

        runBlocking {
            database.dbQuery {
                createStatusoppdateringer(listOf(statusoppdatering1, statusoppdatering2))
            }

            val statusoppdatering1FraDb = database.dbQuery { getStatusoppdateringByEventId(statusoppdatering1.eventId) }
            val statusoppdatering2FraDb = database.dbQuery { getStatusoppdateringByEventId(statusoppdatering2.eventId) }

            statusoppdatering1FraDb.eventId `should be equal to` statusoppdatering1.eventId
            statusoppdatering2FraDb.eventId `should be equal to` statusoppdatering2.eventId

            database.dbQuery { deleteStatusoppdateringWithEventId(statusoppdatering1.eventId) }
            database.dbQuery { deleteStatusoppdateringWithEventId(statusoppdatering2.eventId) }
        }
    }

    @Test
    fun `Skal telle det totale antall statusoppdateringer`() {
        runBlocking {
            database.dbQuery {
                countTotalNumberOfEvents(EventType.STATUSOPPDATERING_INTERN)
            }
        } `should be equal to` allEvents.size.toLong()
    }
}
