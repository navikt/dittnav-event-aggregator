package no.nav.personbruker.dittnav.eventaggregator.oppgave

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import org.junit.jupiter.api.Test

class OppgaveQueriesTest {

    private val database = LocalPostgresDatabase.migratedDb()

    @Test
    fun `Skal haandtere at prefererteKanaler er tom`() {
        val oppgave = OppgaveTestData.oppgave(aktiv = true, prefererteKanaler = emptyList(), fristUtløpt = null)
        runBlocking {
            database.dbQuery { createOppgave(oppgave) }
            val result = database.dbQuery { getOppgaveByEventId(oppgave.eventId) }
            result.prefererteKanaler.shouldBeEmpty()
        }
    }

    @Test
    fun `Finner utgåtte oppgaver og setter inaktiv`() {
        runBlocking {
            val expiredOppgave = OppgaveTestData.oppgave(
                synligFremTil = LocalDateTimeTestHelper.nowAtUtcTruncated().minusDays(1),
                fristUtløpt = null
            )
            database.dbQuery { createOppgave(expiredOppgave) }

            val numberUpdated = database.dbQuery {
                setExpiredOppgaveAsInactive()
            }

            val updatedOppgave = database.dbQuery {
                getOppgaveByEventId(expiredOppgave.eventId)
            }

            updatedOppgave.aktiv shouldBe false
            updatedOppgave.fristUtløpt shouldBe true

            numberUpdated.size shouldBe 1
            numberUpdated.first().eventId shouldBe expiredOppgave.eventId
        }
    }
}
