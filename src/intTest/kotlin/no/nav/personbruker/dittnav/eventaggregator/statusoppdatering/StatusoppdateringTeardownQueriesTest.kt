package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

class StatusoppdateringTeardownQueriesTest {

    private val database = H2Database()

    private val statusoppdatering1 = StatusoppdateringObjectMother.giveMeStatusoppdatering("1", "12345")
    private val statusoppdatering2 = StatusoppdateringObjectMother.giveMeStatusoppdatering("2", "12345")
    private val statusoppdatering3 = StatusoppdateringObjectMother.giveMeStatusoppdatering("3", "12345")

    @Test
    fun `Verifiser at alle rader i Statusoppdateringstabellen slettes`() {
        runBlocking {
            `Opprett tre elementer i databasen`()
            val skalHaElementerIDatabasen = database.dbQuery { getAllStatusoppdatering() }
            skalHaElementerIDatabasen.size `should be equal to` 3

            `Slett alle statusoppdateringelementer fra databasen`()
            val skalIkkeHaElementerIDatabasen = database.dbQuery { getAllStatusoppdatering() }
            skalIkkeHaElementerIDatabasen.isEmpty() `should be equal to` true
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
