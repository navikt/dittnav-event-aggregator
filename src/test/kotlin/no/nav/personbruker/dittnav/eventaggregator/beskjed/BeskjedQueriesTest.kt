package no.nav.personbruker.dittnav.eventaggregator.beskjed

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowTruncatedToMillis
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class BeskjedQueriesTest {

    private val database = LocalPostgresDatabase.migratedDb()

    @AfterEach
    fun cleanup() {
        runBlocking {
            database.dbQuery {
                deleteAllBeskjed()
            }
        }
    }

    @Test
    fun `Skal haandtere at prefererteKanaler er tom`() {
        val beskjed = BeskjedTestData.beskjed(eksternVarsling = true, prefererteKanaler = emptyList())
        runBlocking {
            database.dbQuery { createBeskjed(beskjed) }
            val result = database.dbQuery { getBeskjedByEventId(beskjed.eventId) }
            result.prefererteKanaler.shouldBeEmpty()
        }
    }

    @Test
    fun `Finner utgått beskjeder`() {
        runBlocking {
            val expiredBeskjed = BeskjedTestData.beskjed(synligFremTil = nowTruncatedToMillis().minusDays(1))
            database.dbQuery { createBeskjed(expiredBeskjed) }

            val numberUpdated = database.dbQuery {
                setExpiredBeskjedAsInactive()
            }

            val updatedBeskjed = database.dbQuery {
                getBeskjedByEventId(expiredBeskjed.eventId)
            }

            numberUpdated shouldBe 1
            updatedBeskjed.aktiv shouldBe false
        }
    }
}
