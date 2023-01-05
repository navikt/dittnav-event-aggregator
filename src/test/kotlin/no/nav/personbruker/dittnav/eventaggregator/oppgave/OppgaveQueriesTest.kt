package no.nav.personbruker.dittnav.eventaggregator.oppgave

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveTestData
import no.nav.personbruker.dittnav.eventaggregator.oppgave.createOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.getOppgaveByEventId
import no.nav.personbruker.dittnav.eventaggregator.oppgave.setExpiredOppgaveAsInactive
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import org.junit.jupiter.api.Test

class OppgaveQueriesTest {

    private val database = LocalPostgresDatabase.migratedDb()

    @Test
    fun `Skal haandtere at prefererteKanaler er tom`() {
        val oppgave = OppgaveTestData.oppgave(aktiv = true, prefererteKanaler = emptyList())
        runBlocking {
            database.dbQuery { createOppgave(oppgave) }
            val result = database.dbQuery { getOppgaveByEventId(oppgave.eventId) }
            result.prefererteKanaler.shouldBeEmpty()
        }
    }

    @Test
    fun `Finner utgåtte oppgaver`() {
        runBlocking {
            val expiredOppgave = OppgaveTestData.oppgave(synligFremTil = LocalDateTimeTestHelper.nowTruncatedToMillis().minusDays(1))
            database.dbQuery { createOppgave(expiredOppgave) }

            val numberUpdated = database.dbQuery {
                setExpiredOppgaveAsInactive()
            }

            val updatedOppgave = database.dbQuery {
                getOppgaveByEventId(expiredOppgave.eventId)
            }

            numberUpdated.size shouldBe 1
            numberUpdated.first() shouldBe expiredOppgave.eventId
            updatedOppgave.aktiv shouldBe false
            updatedOppgave.fristUtløpt shouldBe true
        }
    }
}
