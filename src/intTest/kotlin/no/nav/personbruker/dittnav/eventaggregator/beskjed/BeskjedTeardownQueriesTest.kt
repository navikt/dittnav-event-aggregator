package no.nav.personbruker.dittnav.eventaggregator.beskjed

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.junit.jupiter.api.Test

class BeskjedTeardownQueriesTest {

    val database = H2Database()

    val beskjed1 = BeskjedObjectMother.createBeskjed("1", "12345")
    val beskjed2 = BeskjedObjectMother.createBeskjed("2", "12345")
    val beskjed3 = BeskjedObjectMother.createBeskjed("3", "12345")

    @Test
    fun `Verifiser at alle rader i Beskjedstabellen slettes`() {
        runBlocking {
            `Opprett tre elementer i databasen`()
            val skalHaElementerIDatabasen = database.dbQuery { getAllBeskjed() }
            skalHaElementerIDatabasen.size `should be equal to` 3

            `Slett alle informasonselementer fra databasen`()
            val skalIkkeHaElementerIDatabasen = database.dbQuery { getAllBeskjed() }
            skalIkkeHaElementerIDatabasen.isEmpty() `should equal` true
        }
    }

    private suspend fun `Opprett tre elementer i databasen`() {
        database.dbQuery {
            createBeskjed(beskjed1)
            createBeskjed(beskjed2)
            createBeskjed(beskjed3)
        }
    }

    private suspend fun `Slett alle informasonselementer fra databasen`() {
        database.dbQuery {
            deleteAllBeskjed()
        }
    }

}
