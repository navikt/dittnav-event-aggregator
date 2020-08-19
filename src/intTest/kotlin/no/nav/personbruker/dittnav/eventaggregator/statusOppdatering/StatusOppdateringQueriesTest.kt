package no.nav.personbruker.dittnav.eventaggregator.statusOppdatering

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.countTotalNumberOfEvents
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import org.amshove.kluent.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.sql.SQLException

class StatusOppdateringQueriesTest {

    private val database = H2Database()

    private val statusOppdatering1: StatusOppdatering
    private val statusOppdatering2: StatusOppdatering
    private val statusOppdatering3: StatusOppdatering
    private val statusOppdatering4: StatusOppdatering

    private val systembruker = "dummySystembruker"
    private val fodselsnummer = "12345"
    private val eventId = "2"

    private val allEvents: List<StatusOppdatering>
    private val allEventsForSingleUser: List<StatusOppdatering>

    init {
        statusOppdatering1 = createStatusOppdatering("1", "12345")
        statusOppdatering2 = createStatusOppdatering("2", "12345")
        statusOppdatering3 = createStatusOppdatering("3", "12345")
        statusOppdatering4 = createStatusOppdatering("4", "6789")
        allEvents = listOf(statusOppdatering1, statusOppdatering2, statusOppdatering3, statusOppdatering4)
        allEventsForSingleUser = listOf(statusOppdatering1, statusOppdatering2, statusOppdatering3)
    }

    private fun createStatusOppdatering(eventId: String, fodselsnummer: String): StatusOppdatering {
        val statusOppdatering = StatusOppdateringObjectMother.giveMeStatusOppdatering(eventId, fodselsnummer)
        return runBlocking {
            database.dbQuery {
                createStatusOppdatering(statusOppdatering).entityId.let {
                    statusOppdatering.copy(id = it)
                }
            }
        }
    }

    @AfterAll
    fun tearDown() {
        runBlocking {
            database.dbQuery { deleteAllStatusOppdatering() }
        }
    }

    @Test
    fun `Finner alle cachede StatusOppdatering-eventer`() {
        runBlocking {
            val result = database.dbQuery { getAllStatusOppdatering() }
            result.size `should be equal to` allEvents.size
            result `should contain all` allEvents
        }
    }

    @Test
    fun `Finner cachet StatusOppdatering-event med Id`() {
        runBlocking {
            val result = database.dbQuery { statusOppdatering2.id?.let { getStatusOppdateringById(it) } }
            result `should equal` statusOppdatering2
        }
    }

    @Test
    fun `Kaster Exception hvis StatusOppdatering-event med Id ikke finnes`() {
        invoking {
            runBlocking {
                database.dbQuery { getStatusOppdateringById(999) }
            }
        } shouldThrow SQLException::class `with message` "Found no rows"
    }

    @Test
    fun `Finner cachede StatusOppdaterings-eventer for fodselsnummer`() {
        runBlocking {
            val result = database.dbQuery { getStatusOppdateringByFodselsnummer(fodselsnummer) }
            result.size `should be equal to` 3
            result `should contain all` allEventsForSingleUser
        }
    }

    @Test
    fun `Returnerer tom liste hvis StatusOppdaterings-eventer for fodselsnummer ikke finnes`() {
        runBlocking {
            val result = database.dbQuery { getStatusOppdateringByFodselsnummer("-1") }
            result.isEmpty() `should be equal to` true
        }
    }

    @Test
    fun `Finner cachet StatusOppdatering-event med eventId`() {
        runBlocking {
            val result = database.dbQuery { getStatusOppdateringByEventId(eventId) }
            result `should equal` statusOppdatering2
        }
    }

    @Test
    fun `Kaster Exception hvis StatusOppdatering-event med eventId ikke finnes`() {
        invoking {
            runBlocking {
                database.dbQuery { getStatusOppdateringByEventId("-1") }
            }
        } shouldThrow SQLException::class `with message` "Found no rows"
    }

    @Test
    fun `Skal skrive eventer i batch`() {
        val statusOppdatering1 = StatusOppdateringObjectMother.giveMeStatusOppdatering("b-1", "123")
        val statusOppdatering2 = StatusOppdateringObjectMother.giveMeStatusOppdatering("b-2", "123")

        runBlocking {
            database.dbQuery {
                createStatusOppdateringer(listOf(statusOppdatering1, statusOppdatering2))
            }

            val statusOppdatering1FraDb = database.dbQuery { getStatusOppdateringByEventId(statusOppdatering1.eventId) }
            val statusOppdatering2FraDb = database.dbQuery { getStatusOppdateringByEventId(statusOppdatering2.eventId) }

            statusOppdatering1FraDb.eventId `should equal` statusOppdatering1.eventId
            statusOppdatering2FraDb.eventId `should equal` statusOppdatering2.eventId

            database.dbQuery { deleteStatusOppdateringWithEventId(statusOppdatering1.eventId) }
            database.dbQuery { deleteStatusOppdateringWithEventId(statusOppdatering2.eventId) }
        }
    }

    @Test
    fun `Skal telle det totale antall statusOppdateringer`() {
        runBlocking {
            database.dbQuery {
                countTotalNumberOfEvents(EventType.STATUSOPPDATERING)
            }
        } `should be equal to` allEvents.size.toLong()
    }
}
