package no.nav.personbruker.dittnav.eventaggregator.database.entity

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.entity.deleteAllInformasjon
import no.nav.personbruker.dittnav.eventaggregator.entity.deleteAllRowsInInformasjon
import no.nav.personbruker.dittnav.eventaggregator.entity.objectmother.InformasjonObjectMother
import org.amshove.kluent.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.sql.SQLException

class InformasjonQueriesTest {

    private val database = H2Database()

    private val informasjon1: Informasjon
    private val informasjon2: Informasjon
    private val informasjon3: Informasjon
    private val informasjon4: Informasjon

    private val allEvents: List<Informasjon>
    private val allEventsForSingleUser: List<Informasjon>

    init {
        informasjon1 = createInformasjon("1", "12345")
        informasjon2 = createInformasjon("2", "12345")
        informasjon3 = createInformasjon("3", "12345")
        informasjon4 = createInformasjon("4", "6789")
        allEvents = listOf(informasjon1, informasjon2, informasjon3, informasjon4)
        allEventsForSingleUser = listOf(informasjon1, informasjon2, informasjon3)
    }

    private fun createInformasjon(eventId: String, aktorId: String): Informasjon {
        var informasjon = InformasjonObjectMother.createInformasjon(eventId, aktorId)
        runBlocking {
            database.dbQuery {
                var generatedId = createInformasjon(informasjon)
                informasjon = informasjon.copy(id = generatedId)
            }
        }
        return informasjon
    }

    @AfterAll
    fun tearDown() {
        runBlocking {
            database.dbQuery { deleteAllInformasjon() }
        }
    }

    @AfterAll
    fun `tear down`() {
        runBlocking {
            database.dbQuery {
                deleteAllRowsInInformasjon()
            }
        }
    }

    @Test
    fun `Finner alle cachede Informasjon-eventer`() {
        runBlocking {
            val result = database.dbQuery { getAllInformasjon() }
            result.size `should be equal to` allEvents.size
            result `should contain all` allEvents
        }
    }

    @Test
    fun `Finner alle aktive cachede Informasjon-eventer`() {
        runBlocking {
            database.dbQuery { setInformasjonAktiv("2", false) }
            val result = database.dbQuery { getAllInformasjonByAktiv(true) }
            result `should contain all` listOf(informasjon1, informasjon3, informasjon4)
            result `should not contain` informasjon2
            database.dbQuery { setInformasjonAktiv("2", true) }
        }
    }

    @Test
    fun `Finner cachet Informasjon-event med Id`() {
        runBlocking {
            val result = database.dbQuery { informasjon2.id?.let { getInformasjonById(it) } }
            result `should equal` informasjon2
        }
    }

    @Test
    fun `Kaster Exception hvis Informasjon-event med Id ikke finnes`() {
        invoking {
            runBlocking {
                database.dbQuery { getInformasjonById(999) }
            }
        } shouldThrow SQLException::class `with message` "Found no rows"
    }

    @Test
    fun `Finner cachede Informasjons-eventer for aktorID`() {
        runBlocking {
            val result = database.dbQuery { getInformasjonByAktorId("12345") }
            result.size `should be equal to` 3
            result `should contain all` allEventsForSingleUser
        }
    }

    @Test
    fun `Returnerer tom liste hvis Informasjons-eventer for aktorID ikke finnes`() {
        runBlocking {
            val result = database.dbQuery { getInformasjonByAktorId("-1") }
            result.isEmpty() `should be equal to` true
        }
    }

    @Test
    fun `Finner cachet Informasjon-event med eventId`() {
        runBlocking {
            val result = database.dbQuery { getInformasjonByEventId("2") }
            result `should equal` informasjon2
        }
    }

    @Test
    fun `Kaster Exception hvis Informasjon-event med eventId ikke finnes`() {
        invoking {
            runBlocking {
                database.dbQuery { getInformasjonByEventId("-1") }
            }
        } shouldThrow SQLException::class `with message` "Found no rows"
    }
}
