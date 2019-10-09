package no.nav.personbruker.dittnav.eventaggregator.database.entity

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.entity.deleteAllRowsInInformasjon
import no.nav.personbruker.dittnav.eventaggregator.entity.objectmother.InformasjonObjectMother
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.junit.jupiter.api.Test

class InformasjonTeardownQueriesTest {

    val database = H2Database()

    val informasjon1 = InformasjonObjectMother.createInformasjon("1", "12345")
    val informasjon2 = InformasjonObjectMother.createInformasjon("2", "12345")
    val informasjon3 = InformasjonObjectMother.createInformasjon("3", "12345")

    @Test
    fun `Verifiser at alle rader i informasjonstabellen slettes`() {
        runBlocking {
            `Opprett tre elementer i databasen`()
            val skalHaElementerIDatabasen = database.dbQuery { getAllInformasjon() }
            skalHaElementerIDatabasen.size `should be equal to` 3

            `Slett alle informasonselementer fra databasen`()
            val skalIkkeHaElementerIDatabasen = database.dbQuery { getAllInformasjon() }
            skalIkkeHaElementerIDatabasen.isEmpty() `should equal` true
        }
    }

    private suspend fun `Opprett tre elementer i databasen`() {
        database.dbQuery {
            createInformasjon(informasjon1)
            createInformasjon(informasjon2)
            createInformasjon(informasjon3)
        }
    }

    private suspend fun `Slett alle informasonselementer fra databasen`() {
        database.dbQuery {
            deleteAllRowsInInformasjon()
        }
    }

}
