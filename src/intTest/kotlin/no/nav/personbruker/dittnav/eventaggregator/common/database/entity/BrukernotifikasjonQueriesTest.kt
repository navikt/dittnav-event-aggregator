package no.nav.personbruker.dittnav.eventaggregator.common.database.entity

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.informasjon.InformasjonObjectMother
import no.nav.personbruker.dittnav.eventaggregator.informasjon.createInformasjon
import no.nav.personbruker.dittnav.eventaggregator.informasjon.deleteAllInformasjon
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksObjectMother
import no.nav.personbruker.dittnav.eventaggregator.innboks.createInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.deleteAllInnboks
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveObjectMother
import no.nav.personbruker.dittnav.eventaggregator.oppgave.createOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.deleteAllOppgave
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain all`
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class BrukernotifikasjonQueriesTest {

    private val database = H2Database()
    private val oppgave1 = OppgaveObjectMother.createOppgave("1", "12")
    private val informasjon1 = InformasjonObjectMother.createInformasjon("2", "12")
    private val innboks1 = InnboksObjectMother.createInnboks("3", "12")
    private val brukernotifikasjon1 = Brukernotifikasjon("1", "DittNav", EventType.OPPGAVE)
    private val brukernotifikasjon2 = Brukernotifikasjon("2", "DittNav", EventType.INFORMASJON)
    private val brukernotifikasjon3 = Brukernotifikasjon("3", "DittNav", EventType.INNBOKS)
    private val allBrukernotifikasjonEvents = listOf(brukernotifikasjon1, brukernotifikasjon2, brukernotifikasjon3)

    init {
        runBlocking {
            database.dbQuery {
                createOppgave(oppgave1)
                createInformasjon(informasjon1)
                createInnboks(innboks1)
            }
        }
    }

    @AfterAll
    fun tearDown() {
        runBlocking {
            database.dbQuery {
                deleteAllInformasjon()
                deleteAllOppgave()
                deleteAllInnboks()
            }
        }
    }

    @Test
    fun `Finner alle aggregerte Brukernotifikasjon-eventer fra databaseview`() {
        runBlocking {
            val result = database.dbQuery { getAllBrukernotifikasjonFromView() }
            result.size `should be equal to` 3
            result `should contain all` allBrukernotifikasjonEvents
        }
    }
}
