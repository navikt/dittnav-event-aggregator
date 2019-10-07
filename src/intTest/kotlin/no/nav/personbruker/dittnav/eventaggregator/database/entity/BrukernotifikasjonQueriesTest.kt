package no.nav.personbruker.dittnav.eventaggregator.database.entity

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.config.INFORMASJON
import no.nav.personbruker.dittnav.eventaggregator.config.OPPGAVE
import no.nav.personbruker.dittnav.eventaggregator.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.entity.deleteAllInformasjon
import no.nav.personbruker.dittnav.eventaggregator.entity.deleteAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.entity.objectmother.InformasjonObjectMother
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain all`
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class BrukernotifikasjonQueriesTest {

    private val database = H2Database()
    private val oppgave1 = OppgaveObjectMother.createOppgave(1, "12")
    private val informasjon1 = InformasjonObjectMother.createInformasjon(2, "12")
    private val brukernotifikasjon1 = Brukernotifikasjon("1", "DittNav", OPPGAVE)
    private val brukernotifikasjon2 = Brukernotifikasjon("2", "DittNav", INFORMASJON)
    private val allBrukernotifikasjonEvents = listOf(brukernotifikasjon1, brukernotifikasjon2)

    init {
        runBlocking {
            database.dbQuery {
                createOppgave(oppgave1)
                createInformasjon(informasjon1)
            }
        }
    }

    @AfterAll
    fun tearDown() {
        runBlocking {
            database.dbQuery {
                deleteAllInformasjon()
                deleteAllOppgave()
            }
        }
    }

    @Test
    fun `Finner alle aggregerte Brukernotifikasjon-eventer fra databaseview`() {
        runBlocking {
            val result = database.dbQuery { getAllBrukernotifikasjonerFromView() }
            result.size `should be equal to` 2
            result `should contain all` allBrukernotifikasjonEvents
        }
    }
}
