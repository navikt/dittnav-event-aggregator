package no.nav.personbruker.dittnav.eventaggregator.beskjed

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowTruncatedToMillis
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.countTotalNumberOfEvents
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import org.junit.jupiter.api.Test
import java.sql.SQLException

class BeskjedQueriesTest {

    private val database = LocalPostgresDatabase.migratedDb()

    private val beskjed1: Beskjed
    private val beskjed2: Beskjed
    private val beskjed3: Beskjed
    private val beskjed4: Beskjed
    private val expiredBeskjed: Beskjed
    private val beskjedWithOffsetForstBehandlet: Beskjed
    private val inaktivBeskjed: Beskjed

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
        inaktivBeskjed = createInaktivBeskjed("6", "12345")
        allEvents = listOf(beskjed1, beskjed2, beskjed3, beskjed4, expiredBeskjed, beskjedWithOffsetForstBehandlet, inaktivBeskjed)
        allEventsForSingleUser = listOf(beskjed1, beskjed2, beskjed3, beskjedWithOffsetForstBehandlet, inaktivBeskjed)
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
            .copy(synligFremTil = nowTruncatedToMillis().minusDays(1))
        return runBlocking {
            database.dbQuery {
                createBeskjed(beskjed).entityId.let {
                    beskjed.copy(id = it)
                }
            }
        }
    }

    private fun createBeskjedWithOffsetForstBehandlet(eventId: String, fodselsnummer: String): Beskjed {
        val offsetDate = nowTruncatedToMillis().minusDays(1)
        val beskjed = BeskjedObjectMother.giveMeBeskjedWithForstBehandlet(eventId, fodselsnummer, offsetDate)
        return runBlocking {
            database.dbQuery {
                createBeskjed(beskjed).entityId.let {
                    beskjed.copy(id = it)
                }
            }
        }
    }

    private fun createInaktivBeskjed(eventId: String, fodselsnummer: String): Beskjed {
        val beskjed = BeskjedObjectMother.giveMeBeskjed(
            eventId = eventId,
            fodselsnummer = fodselsnummer,
            aktiv = false
        )

        return runBlocking {
            database.dbQuery {
                val generatedId = createBeskjed(beskjed).entityId

                beskjed.copy(id = generatedId)
            }
        }
    }

    @Test
    fun `Finner alle aktive cachede Beskjed-eventer`() {
        runBlocking {
            val result = database.dbQuery { getAllBeskjedByAktiv(true) }
            result shouldContainAll listOf(beskjed1, beskjed2, beskjed3, beskjed4)
            result shouldNotContain inaktivBeskjed
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
            result.size shouldBe 5
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
    fun `Finner utgått beskjeder`() {
        runBlocking {
            val numberUpdated = database.dbQuery {
                setExpiredBeskjedAsInactive()
            }

            val updatedOppave = database.dbQuery {
                getBeskjedByEventId(expiredBeskjed.eventId)
            }

            numberUpdated shouldBe 1
            updatedOppave.aktiv shouldBe false
        }
    }
}
