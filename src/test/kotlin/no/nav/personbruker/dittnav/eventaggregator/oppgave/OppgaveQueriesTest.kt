package no.nav.personbruker.dittnav.eventaggregator.oppgave

import io.kotest.matchers.collections.shouldBeEmpty
import kotlinx.coroutines.runBlocking
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
}
