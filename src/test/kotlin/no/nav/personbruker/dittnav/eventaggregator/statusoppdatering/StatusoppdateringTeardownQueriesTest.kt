package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import org.junit.jupiter.api.Test

class StatusoppdateringTeardownQueriesTest {

    private val database = LocalPostgresDatabase.migratedDb()

    private val statusoppdatering1 = StatusoppdateringObjectMother.giveMeStatusoppdatering("1", "12345")
    private val statusoppdatering2 = StatusoppdateringObjectMother.giveMeStatusoppdatering("2", "12345")
    private val statusoppdatering3 = StatusoppdateringObjectMother.giveMeStatusoppdatering("3", "12345")

    @Test
    fun `Verifiser at alle rader i Statusoppdateringstabellen slettes`() {
        runBlocking {
            `Opprett tre elementer i databasen`()
            delay(100)
            val skalHaElementerIDatabasen = database.dbQuery { getAllStatusoppdatering() }
            skalHaElementerIDatabasen.size shouldBe 3

            `Slett alle statusoppdateringelementer fra databasen`()
            val skalIkkeHaElementerIDatabasen = database.dbQuery { getAllStatusoppdatering() }
            skalIkkeHaElementerIDatabasen.isEmpty() shouldBe true
        }
    }

    private suspend fun `Opprett tre elementer i databasen`() {
        database.dbQuery {
            createStatusoppdatering(statusoppdatering1)
            createStatusoppdatering(statusoppdatering2)
            createStatusoppdatering(statusoppdatering3)
        }
    }

    private suspend fun `Slett alle statusoppdateringelementer fra databasen`() {
        database.dbQuery {
            deleteAllStatusoppdatering()
        }
    }

}
