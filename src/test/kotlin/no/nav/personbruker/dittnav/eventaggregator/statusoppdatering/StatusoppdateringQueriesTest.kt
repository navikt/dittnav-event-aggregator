package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.countTotalNumberOfEvents
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.sql.SQLException
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class StatusoppdateringQueriesTest {

    private val database = LocalPostgresDatabase.migratedDb()

    private val statusoppdatering1: Statusoppdatering
    private val statusoppdatering2: Statusoppdatering
    private val statusoppdatering3: Statusoppdatering
    private val statusoppdatering4: Statusoppdatering

    private val statusoppdateringWithOffsetForstBehandlet: Statusoppdatering

    private val fodselsnummer = "12345"
    private val eventId = "2"

    private val allEvents: List<Statusoppdatering>
    private val allEventsForSingleUser: List<Statusoppdatering>

    init {
        statusoppdatering1 = createStatusoppdatering("1", "12345")
        statusoppdatering2 = createStatusoppdatering("2", "12345")
        statusoppdatering3 = createStatusoppdatering("3", "12345")
        statusoppdatering4 = createStatusoppdatering("4", "6789")

        statusoppdateringWithOffsetForstBehandlet = createStatusoppdateringWithOffsetForstBehandlet("5", "12345")

        allEvents = listOf(statusoppdatering1, statusoppdatering2, statusoppdatering3, statusoppdatering4, statusoppdateringWithOffsetForstBehandlet)
        allEventsForSingleUser = listOf(statusoppdatering1, statusoppdatering2, statusoppdatering3, statusoppdateringWithOffsetForstBehandlet)
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

    private fun createStatusoppdateringWithOffsetForstBehandlet(eventId: String, fodselsnummer: String): Statusoppdatering {
        val offsetDate = LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS)
        var statusoppdatering = StatusoppdateringObjectMother.giveMeStatusoppdateringWithForstBehandlet(eventId, fodselsnummer, offsetDate)
        runBlocking {
            database.dbQuery {
                val generatedId = createStatusoppdatering(statusoppdatering).entityId
                statusoppdatering = statusoppdatering.copy(id = generatedId)
            }
        }
        return statusoppdatering
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
            result.size shouldBe allEvents.size
            result shouldContainAll allEvents
        }
    }

    @Test
    fun `Finner cachet Statusoppdatering-event med Id`() {
        runBlocking {
            val result = database.dbQuery { statusoppdatering2.id?.let { getStatusoppdateringById(it) } }
            result shouldBe statusoppdatering2
        }
    }

    @Test
    fun `Kaster Exception hvis Statusoppdatering-event med Id ikke finnes`() {
        shouldThrow<SQLException> {
            runBlocking {
                database.dbQuery { getStatusoppdateringById(999) }
            }
        }.message shouldBe "Found no rows"
    }

    @Test
    fun `Finner cachede Statusoppdaterings-eventer for fodselsnummer`() {
        runBlocking {
            val result = database.dbQuery { getStatusoppdateringByFodselsnummer(fodselsnummer) }
            result.size shouldBe 4
            result shouldContainAll allEventsForSingleUser
        }
    }

    @Test
    fun `Returnerer tom liste hvis Statusoppdaterings-eventer for fodselsnummer ikke finnes`() {
        runBlocking {
            val result = database.dbQuery { getStatusoppdateringByFodselsnummer("-1") }
            result.isEmpty() shouldBe true
        }
    }

    @Test
    fun `Finner cachet Statusoppdatering-event med eventId`() {
        runBlocking {
            val result = database.dbQuery { getStatusoppdateringByEventId(eventId) }
            result shouldBe statusoppdatering2
        }
    }

    @Test
    fun `Kaster Exception hvis Statusoppdatering-event med eventId ikke finnes`() {
        shouldThrow<SQLException> {
            runBlocking {
                database.dbQuery { getStatusoppdateringByEventId("-1") }
            }
        }.message shouldBe "Found no rows"
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

            statusoppdatering1FraDb.eventId shouldBe statusoppdatering1.eventId
            statusoppdatering2FraDb.eventId shouldBe statusoppdatering2.eventId

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
        } shouldBe allEvents.size.toLong()
    }
}
