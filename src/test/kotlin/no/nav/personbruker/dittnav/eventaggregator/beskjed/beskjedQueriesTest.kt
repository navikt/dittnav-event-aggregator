package no.nav.personbruker.dittnav.eventaggregator.beskjed

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.countTotalNumberOfEvents
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.countTotalNumberOfEventsByActiveStatus
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.done.DoneObjectMother
import org.junit.jupiter.api.Test
import java.sql.SQLException
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class beskjedQueriesTest {

    private val database = LocalPostgresDatabase.migratedDb()

    private val beskjed1: Beskjed
    private val beskjed2: Beskjed
    private val beskjed3: Beskjed
    private val beskjed4: Beskjed
    private val expiredBeskjed: Beskjed
    private val beskjedWithOffsetForstBehandlet: Beskjed

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
        expiredBeskjed = createExpiredBeskjed("123", "4567")
        beskjedWithOffsetForstBehandlet = createBeskjedWithOffsetForstBehandlet("5", "12345")
        allEvents = listOf(beskjed1, beskjed2, beskjed3, beskjed4, expiredBeskjed, beskjedWithOffsetForstBehandlet)
        allEventsForSingleUser = listOf(beskjed1, beskjed2, beskjed3, beskjedWithOffsetForstBehandlet)
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

    private fun createExpiredBeskjed(eventId: String, fodselsnummer: String): Beskjed {
        val beskjed = BeskjedObjectMother.giveMeAktivBeskjed(eventId, fodselsnummer)
            .copy(synligFremTil = LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS))
        return runBlocking {
            database.dbQuery {
                createBeskjed(beskjed).entityId.let {
                    beskjed.copy(id = it)
                }
            }
        }
    }

    private fun createBeskjedWithOffsetForstBehandlet(eventId: String, fodselsnummer: String): Beskjed {
        val offsetDate = LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS)
        val beskjed = BeskjedObjectMother.giveMeBeskjedWithForstBehandlet(eventId, fodselsnummer, offsetDate)
        return runBlocking {
            database.dbQuery {
                createBeskjed(beskjed).entityId.let {
                    beskjed.copy(id = it)
                }
            }
        }
    }

    @Test
    fun `Finner alle cachede Beskjed-eventer`() {
        runBlocking {
            val result = database.dbQuery { getAllBeskjed() }
            result.size shouldBe allEvents.size
            result.toSet() shouldBe allEvents.toSet()
        }
    }

    @Test
    fun `Finner alle aktive cachede Beskjed-eventer`() {
        val doneEvent = DoneObjectMother.giveMeDone(eventId, systembruker, fodselsnummer)
        runBlocking {
            database.dbQuery { setBeskjederAktivflagg(listOf(doneEvent), false) }
            val result = database.dbQuery { getAllBeskjedByAktiv(true) }
            result shouldContainAll listOf(beskjed1, beskjed3, beskjed4)
            result shouldNotContain beskjed2
            database.dbQuery { setBeskjederAktivflagg(listOf(doneEvent), true) }
        }
    }

    @Test
    fun `Finner cachet Beskjed-event med Id`() {
        runBlocking {
            val result = database.dbQuery { beskjed2.id?.let { getBeskjedById(it) } }
            result shouldBe beskjed2
        }
    }

    @Test
    fun `Kaster Exception hvis Beskjed-event med Id ikke finnes`() {
        shouldThrow<SQLException> {
            runBlocking {
                database.dbQuery { getBeskjedById(999) }
            }
        }.message shouldBe "Found no rows"
    }

    @Test
    fun `Finner cachede Beskjeds-eventer for fodselsnummer`() {
        runBlocking {
            val result = database.dbQuery { getBeskjedByFodselsnummer(fodselsnummer) }
            result.size shouldBe 4
            result shouldContainAll allEventsForSingleUser
        }
    }

    @Test
    fun `Returnerer tom liste hvis Beskjeds-eventer for fodselsnummer ikke finnes`() {
        runBlocking {
            val result = database.dbQuery { getBeskjedByFodselsnummer("-1") }
            result.isEmpty() shouldBe true
        }
    }

    @Test
    fun `Finner cachet Beskjed-event med eventId`() {
        runBlocking {
            val result = database.dbQuery { getBeskjedByEventId(eventId) }
            result shouldBe beskjed2
        }
    }

    @Test
    fun `Kaster Exception hvis Beskjed-event med eventId ikke finnes`() {
        shouldThrow<SQLException> {
            runBlocking {
                database.dbQuery { getBeskjedByEventId("-1") }
            }
        }.message shouldBe "Found no rows"
    }

    @Test
    fun `Skal haandtere at prefererteKanaler er tom`() {
        val beskjed = BeskjedObjectMother.giveMeAktivBeskjedWithEksternVarslingAndPrefererteKanaler(true, emptyList())
        runBlocking {
            database.dbQuery { createBeskjed(beskjed) }
            val result = database.dbQuery { getBeskjedByEventId(beskjed.eventId) }
            result.prefererteKanaler.shouldBeEmpty()
            database.dbQuery { deleteBeskjedWithEventId(beskjed.eventId) }
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

            beskjed1FraDb.eventId shouldBe beskjed1.eventId
            beskjed2FraDb.eventId shouldBe beskjed2.eventId

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
        } shouldBe allEvents.size.toLong()
    }

    @Test
    fun `Skal telle det totale antall aktive beskjeder`() {
        runBlocking {
            database.dbQuery {
                countTotalNumberOfEventsByActiveStatus(EventType.BESKJED_INTERN, true)
            }
        } shouldBe allEvents.size.toLong()
    }

    @Test
    fun `Skal telle det totale antall inaktive beskjeder`() {
        runBlocking {
            database.dbQuery {
                countTotalNumberOfEventsByActiveStatus(EventType.BESKJED_INTERN, false)
            }
        } shouldBe 0
    }

    @Test
    fun `Finner utgått beskjeder`() {
        runBlocking {
            val result = database.dbQuery {
                getExpiredBeskjedFromCursor()
            }

            result shouldHaveSize 1
            result shouldContain expiredBeskjed
        }
    }
}
