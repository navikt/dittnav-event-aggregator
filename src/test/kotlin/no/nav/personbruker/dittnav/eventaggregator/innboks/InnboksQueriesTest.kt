package no.nav.personbruker.dittnav.eventaggregator.innboks

import io.kotest.matchers.collections.shouldBeEmpty
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import org.junit.jupiter.api.Test

class InnboksQueriesTest {
    private val database = LocalPostgresDatabase.migratedDb()

    @Test
    fun `Skal haandtere at prefererteKanaler er tom`() {
        val innboks = InnboksTestData.innboks(aktiv = true, prefererteKanaler =  emptyList())
        runBlocking {
            database.dbQuery { createInnboks(innboks) }
            val result = database.dbQuery { getInnboksByEventId(innboks.eventId) }
            result.prefererteKanaler.shouldBeEmpty()
        }
    }
}
