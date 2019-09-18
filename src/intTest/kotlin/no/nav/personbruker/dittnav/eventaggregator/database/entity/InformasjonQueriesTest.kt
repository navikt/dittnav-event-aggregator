package no.nav.personbruker.dittnav.eventaggregator.database.entity

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.entity.objectmother.InformasjonObjectMother
import org.amshove.kluent.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.sql.SQLException

class InformasjonQueriesTest {

    companion object {
        val database = H2Database()

        val informasjon1 = InformasjonObjectMother.createInformasjon(1, "12345")
        val informasjon2 = InformasjonObjectMother.createInformasjon(2, "12345")
        val informasjon3 = InformasjonObjectMother.createInformasjon(3, "12345")
        val informasjon4 = InformasjonObjectMother.createInformasjon(4, "6789")
        val allEvents = listOf(informasjon1, informasjon2, informasjon3, informasjon4)
        val allEventsForSingleUser = listOf(informasjon1, informasjon2, informasjon3)

        @BeforeAll
        @JvmStatic
        fun setup() {
            runBlocking {
                database.dbQuery {
                    createInformasjon(informasjon1)
                    createInformasjon(informasjon2)
                    createInformasjon(informasjon3)
                    createInformasjon(informasjon4)
                }
            }
        }
    }

    @Test
    fun `Finner alle cachede Informasjons-eventer`() {
        runBlocking {

            val result = database.dbQuery { getAllInformasjon() }

            result.size `should be equal to` allEvents.size
            result `should contain all` allEvents
        }
    }

    @Test
    fun `Finner cachet Informasjon-event med ID`() {
        runBlocking {
            val result = database.dbQuery { getInformasjonById(2) }
            result `should equal` informasjon2
        }
    }

    @Test
    fun `Kaster Exception hvis Informasjon-event med ID ikke finnes`() {
        invoking {
            runBlocking {
                database.dbQuery { getInformasjonById(999) }
            }
        } shouldThrow SQLException::class `with message` "Found no rows"
    }

    @Test
    fun `Finner cachede Informasjons-eventer for aktorID`() {
        runBlocking {
            val result = database.dbQuery { getInformasjonByAktorid("12345") }

            result.size `should be equal to` 3
            result `should contain all` allEventsForSingleUser
        }
    }

    @Test
    fun `Returnerer tom liste hvis Informasjons-eventer for aktorID ikke finnes`() {
        runBlocking {
            val result = database.dbQuery { getInformasjonByAktorid("-1") }
            result.isEmpty() `should be equal to` true
        }
    }

}
