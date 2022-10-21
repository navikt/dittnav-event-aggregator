package no.nav.personbruker.dittnav.eventaggregator.oppgave

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowTruncatedToMillis
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import org.junit.jupiter.api.Test
import java.sql.SQLException

class OppgaveQueriesTest {

    private val database = LocalPostgresDatabase.migratedDb()

    private val fodselsnummer1 = "12345"
    private val fodselsnummer2 = "54321"

    private val oppgave1: Oppgave
    private val oppgave2: Oppgave
    private val oppgave3: Oppgave

    private val expiredOppgave: Oppgave
    private val oppgaveWithOffsetForstBehandlet: Oppgave
    private val inaktivOppgave: Oppgave

    private val eventId = "2"

    private val allEvents: List<Oppgave>
    private val allEventsForSingleUser: List<Oppgave>

    init {
        oppgave1 = createOppgave("1", fodselsnummer1)
        oppgave2 = createOppgave("2", fodselsnummer2)
        oppgave3 = createOppgave("3", fodselsnummer1)
        expiredOppgave = createExpiredOppgave("4", "5678")
        oppgaveWithOffsetForstBehandlet = createOppgaveWithOffsetForstBehandlet("5", fodselsnummer1)
        inaktivOppgave = createInaktivOppgave("6", fodselsnummer1)
        allEvents = listOf(oppgave1, oppgave2, oppgave3, expiredOppgave, oppgaveWithOffsetForstBehandlet, inaktivOppgave)
        allEventsForSingleUser = listOf(oppgave1, oppgave3, oppgaveWithOffsetForstBehandlet, inaktivOppgave)
    }

    private fun createOppgave(eventId: String, fodselsnummer: String): Oppgave {
        var oppgave = OppgaveTestData.oppgave(eventId = eventId, fodselsnummer = fodselsnummer)
        runBlocking {
            database.dbQuery {
                val generatedId = createOppgave(oppgave).entityId
                oppgave = oppgave.copy(id = generatedId)
            }
        }
        return oppgave
    }

    private fun createExpiredOppgave(eventId: String, fodselsnummer: String): Oppgave {
        var oppgave = OppgaveTestData.oppgave(eventId = eventId, fodselsnummer = fodselsnummer)
            .copy(synligFremTil = nowTruncatedToMillis().minusDays(1))
        runBlocking {
            database.dbQuery {
                val generatedId = createOppgave(oppgave).entityId
                oppgave = oppgave.copy(id = generatedId)
            }
        }
        return oppgave
    }

    private fun createInaktivOppgave(eventId: String, fodselsnummer: String): Oppgave {
        val oppgave = OppgaveTestData.oppgave(
            eventId = eventId,
            fodselsnummer = fodselsnummer,
            aktiv = false
        )

        return runBlocking {
            database.dbQuery {
                val generatedId = createOppgave(oppgave).entityId

                oppgave.copy(id = generatedId)
            }
        }
    }

    private fun createOppgaveWithOffsetForstBehandlet(eventId: String, fodselsnummer: String): Oppgave {
        val offsetDate = nowTruncatedToMillis().minusDays(1)
        var oppgave = OppgaveTestData.oppgave(eventId = eventId, fodselsnummer = fodselsnummer, forstBehandlet = offsetDate)
        runBlocking {
            database.dbQuery {
                val generatedId = createOppgave(oppgave).entityId
                oppgave = oppgave.copy(id = generatedId)
            }
        }
        return oppgave
    }

    @Test
    fun `Finner alle cachede Oppgave-eventer`() {
        runBlocking {
            val result = database.dbQuery { getAllOppgave() }
            result.size shouldBe allEvents.size
            result.toSet() shouldBe allEvents.toSet()
        }
    }

    @Test
    fun `Finner alle aktive cachede Oppgave-eventer`() {
        runBlocking {
            val result = database.dbQuery { getAllOppgaveByAktiv(true) }
            result shouldContainAll listOf(oppgave1, oppgave2, oppgave3)
            result shouldNotContain inaktivOppgave
        }
    }

    @Test
    fun `Finner alle cachede Oppgave-event for fodselsnummer`() {
        runBlocking {
            val result = database.dbQuery { getOppgaveByFodselsnummer(fodselsnummer1) }
            result.size shouldBe allEventsForSingleUser.size
            result shouldContainAll allEventsForSingleUser
        }
    }

    @Test
    fun `Gir tom liste dersom Oppgave-event med gitt fodselsnummer ikke finnes`() {
        runBlocking {
            val result = database.dbQuery { getOppgaveByFodselsnummer("-1") }
            result.isEmpty() shouldBe true
        }
    }

    @Test
    fun `Finner cachet Oppgave-event for Id`() {
        runBlocking {
            val result = database.dbQuery { oppgave2.id?.let { getOppgaveById(it) } }
            result shouldBe oppgave2
        }
    }

    @Test
    fun `Kaster exception dersom Oppgave-event med Id ikke finnes`() {
        shouldThrow<SQLException> {
            runBlocking {
                database.dbQuery { getOppgaveById(-1) }
            }
        }.message shouldBe "Found no rows"
    }

    @Test
    fun `Finner cachet Oppgave-event med eventId`() {
        runBlocking {
            val result = database.dbQuery { getOppgaveByEventId(eventId) }
            result shouldBe oppgave2
        }
    }

    @Test
    fun `Kaster exception dersom Oppgave-event med eventId ikke finnes`() {
        shouldThrow<SQLException> {
            runBlocking {
                database.dbQuery { getOppgaveByEventId("-1") }
            }
        }.message shouldBe "Found no rows"
    }

    @Test
    fun `Persister ikke entitet dersom rad med samme eventId og produsent finnes`() {
        runBlocking {
            database.dbQuery {
                createOppgave(oppgave1)
                val numberOfEvents = getAllOppgave().size
                createOppgave(oppgave1)
                getAllOppgave().size shouldBe numberOfEvents
            }
        }
    }

    @Test
    fun `Skal haandtere at prefererteKanaler er tom`() {
        val oppgave = OppgaveTestData.oppgave(aktiv = true, prefererteKanaler = emptyList())
        runBlocking {
            database.dbQuery { createOppgave(oppgave) }
            val result = database.dbQuery { getOppgaveByEventId(oppgave.eventId) }
            result.prefererteKanaler.shouldBeEmpty()
            database.dbQuery { deleteOppgaveWithEventId(oppgave.eventId) }
        }
    }
}
