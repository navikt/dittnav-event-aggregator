package no.nav.personbruker.dittnav.eventaggregator.beskjed

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

class BeskjedTeardownQueriesTest {

    private val database = LocalPostgresDatabase()

    private val beskjed1 = BeskjedObjectMother.giveMeAktivBeskjed("1", "12345")
    private val beskjed2 = BeskjedObjectMother.giveMeAktivBeskjed("2", "12345")
    private val beskjed3 = BeskjedObjectMother.giveMeAktivBeskjed("3", "12345")

    @Test
    fun `Verifiser at alle rader i Beskjedstabellen slettes`() {
        runBlocking {
            `Opprett tre elementer i databasen`()
            val skalHaElementerIDatabasen = database.dbQuery { getAllBeskjed() }
            skalHaElementerIDatabasen.size `should be equal to` 3

            `Slett alle beskjedelementer fra databasen`()
            val skalIkkeHaElementerIDatabasen = database.dbQuery { getAllBeskjed() }
            skalIkkeHaElementerIDatabasen.isEmpty() `should be equal to` true
        }
    }

    private suspend fun `Opprett tre elementer i databasen`() {
        database.dbQuery {
            createBeskjed(beskjed1)
            createBeskjed(beskjed2)
            createBeskjed(beskjed3)
        }
    }

    private suspend fun `Slett alle beskjedelementer fra databasen`() {
        database.dbQuery {
            deleteAllBeskjed()
        }
    }

}
