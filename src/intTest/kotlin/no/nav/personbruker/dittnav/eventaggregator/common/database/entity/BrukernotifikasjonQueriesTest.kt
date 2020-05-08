package no.nav.personbruker.dittnav.eventaggregator.common.database.entity

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.beskjed.createBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.common.objectmother.BrukernotifikasjonObjectMother
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
    private val aktivOppgave = OppgaveObjectMother.giveMeAktivOppgave()
    private val aktivBeskjed = BeskjedObjectMother.giveMeAktivBeskjed()
    private val aktivInnboks = InnboksObjectMother.giveMeAktivInnboks()
    private val inaktivOppgave = OppgaveObjectMother.giveMeInaktivOppgave()
    private val inaktivBeskjed = BeskjedObjectMother.giveMeInaktivBeskjed()
    private val inaktivInnboks = InnboksObjectMother.giveMeInaktivInnboks()


    init {
        runBlocking {
            database.dbQuery {
                createOppgave(aktivOppgave)
                createBeskjed(aktivBeskjed)
                createInnboks(aktivInnboks)
                createOppgave(inaktivOppgave)
                createBeskjed(inaktivBeskjed)
                createInnboks(inaktivInnboks)
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
    fun `Finner alle aggregerte Brukernotifikasjon-eventer fra databaseview for eventId-er`() {
        val brukernotifikasjon1 = BrukernotifikasjonObjectMother.giveMeFor(aktivBeskjed)
        val brukernotifikasjon3 = BrukernotifikasjonObjectMother.giveMeFor(aktivOppgave)
        val brukernotifikasjon2 = BrukernotifikasjonObjectMother.giveMeFor(aktivInnboks)
        val aktiveBrukernotifikasjonEventer = listOf(brukernotifikasjon1, brukernotifikasjon2, brukernotifikasjon3)
        val eventIds = aktiveBrukernotifikasjonEventer.map { it.eventId }
        runBlocking {
            val result = database.dbQuery { getBrukernotifikasjonFromViewForEventIds(eventIds) }
            result.size `should be equal to` 3
            result `should contain all` aktiveBrukernotifikasjonEventer
        }
    }

}
