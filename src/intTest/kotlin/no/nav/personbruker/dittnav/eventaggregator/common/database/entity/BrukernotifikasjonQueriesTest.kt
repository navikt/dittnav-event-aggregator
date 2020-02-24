package no.nav.personbruker.dittnav.eventaggregator.common.database.entity

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.beskjed.createBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
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
    private val beskjed1 = BeskjedObjectMother.createBeskjed("2", "12")
    private val innboks1 = InnboksObjectMother.createInnboks("3", "12")
    private val brukernotifikasjon1 = Brukernotifikasjon("1", "DittNAV", EventType.OPPGAVE, "12")
    private val brukernotifikasjon2 = Brukernotifikasjon("2", "DittNAV", EventType.BESKJED, "12")
    private val brukernotifikasjon3 = Brukernotifikasjon("3", "DittNAV", EventType.INNBOKS, "12")
    private val allBrukernotifikasjonEvents = listOf(brukernotifikasjon1, brukernotifikasjon2, brukernotifikasjon3)

    init {
        runBlocking {
            database.dbQuery {
                createOppgave(oppgave1)
                createBeskjed(beskjed1)
                createInnboks(innboks1)
            }
        }
    }

    @AfterAll
    fun tearDown() {
        runBlocking {
            database.dbQuery {
                deleteAllBeskjed()
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
