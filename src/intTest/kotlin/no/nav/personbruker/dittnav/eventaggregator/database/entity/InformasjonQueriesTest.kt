package no.nav.personbruker.dittnav.eventaggregator.database.entity

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.entity.deleteAllInformasjon
import no.nav.personbruker.dittnav.eventaggregator.entity.deleteInformasjonWithEventId
import no.nav.personbruker.dittnav.eventaggregator.entity.objectmother.InformasjonObjectMother
import org.amshove.kluent.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.sql.SQLException

class InformasjonQueriesTest {

    private val database = H2Database()

    private val informasjon1 = InformasjonObjectMother.createInformasjon(1, "12345")
    private val informasjon2 = InformasjonObjectMother.createInformasjon(2, "12345")
    private val informasjon3 = InformasjonObjectMother.createInformasjon(3, "12345")
    private val informasjon4 = InformasjonObjectMother.createInformasjon(4, "6789")
    private val allEvents = listOf(informasjon1, informasjon2, informasjon3, informasjon4)
    private val allEventsForSingleUser = listOf(informasjon1, informasjon2, informasjon3)

    init {
        runBlocking {
            database.dbQuery {
                createInformasjon(informasjon1)
                createInformasjon(informasjon2)
                createInformasjon(informasjon3)
                createInformasjon(informasjon4)
            }
        }
    }

    @AfterAll
    fun tearDown() {
        runBlocking {
            database.dbQuery { deleteAllInformasjon() }
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
            val inaktivInformasjon = InformasjonObjectMother.createInformasjon(5, "12345", false)
            database.dbQuery { createInformasjon(inaktivInformasjon) }
            val result = database.dbQuery { getAllInformasjonByAktiv(true) }
            result `should contain all` allEvents
            result `should not contain` inaktivInformasjon
            database.dbQuery { deleteInformasjonWithEventId(inaktivInformasjon.eventId) }
        }
    }

    @Test
    fun `Finner cachet Informasjon-event med Id`() {
        runBlocking {
            val result = database.dbQuery { getInformasjonById(2) }
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
            val result = database.dbQuery { getInformasjonByEventId(2) }
            result `should equal` informasjon2
        }
    }

    @Test
    fun `Kaster Exception hvis Informasjon-event med eventId ikke finnes`() {
        invoking {
            runBlocking {
                database.dbQuery { getInformasjonByEventId(-1) }
            }
        } shouldThrow SQLException::class `with message` "Found no rows"
    }
}
