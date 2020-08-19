package no.nav.personbruker.dittnav.eventaggregator.statusOppdatering

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.junit.jupiter.api.Test

class StatusOppdateringTeardownQueriesTest {

    private val database = H2Database()

    private val statusOppdatering1 = StatusOppdateringObjectMother.giveMeStatusOppdatering("1", "12345")
    private val statusOppdatering2 = StatusOppdateringObjectMother.giveMeStatusOppdatering("2", "12345")
    private val statusOppdatering3 = StatusOppdateringObjectMother.giveMeStatusOppdatering("3", "12345")

    @Test
    fun `Verifiser at alle rader i StatusOppdateringstabellen slettes`() {
        runBlocking {
            `Opprett tre elementer i databasen`()
            val skalHaElementerIDatabasen = database.dbQuery { getAllStatusOppdatering() }
            skalHaElementerIDatabasen.size `should be equal to` 3

            `Slett alle statusOppdateringelementer fra databasen`()
            val skalIkkeHaElementerIDatabasen = database.dbQuery { getAllStatusOppdatering() }
            skalIkkeHaElementerIDatabasen.isEmpty() `should equal` true
        }
    }

    private suspend fun `Opprett tre elementer i databasen`() {
        database.dbQuery {
            createStatusOppdatering(statusOppdatering1)
            createStatusOppdatering(statusOppdatering2)
            createStatusOppdatering(statusOppdatering3)
        }
    }

    private suspend fun `Slett alle statusOppdateringelementer fra databasen`() {
        database.dbQuery {
            deleteAllStatusOppdatering()
        }
    }

}
