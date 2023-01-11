package no.nav.personbruker.dittnav.eventaggregator.common.brukernotifikasjon

import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedTestData
import no.nav.personbruker.dittnav.eventaggregator.beskjed.createBeskjed
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.done.getBrukernotifikasjonFromViewForEventIds
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksTestData
import no.nav.personbruker.dittnav.eventaggregator.innboks.createInnboks
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveTestData
import no.nav.personbruker.dittnav.eventaggregator.oppgave.createOppgave
import org.junit.jupiter.api.Test

class BrukernotifikasjonQueriesTest {

    private val database = LocalPostgresDatabase.migratedDb()
    private val aktivOppgave = OppgaveTestData.oppgave(fristUtløpt = null)
    private val aktivBeskjed = BeskjedTestData.beskjed()
    private val aktivInnboks = InnboksTestData.innboks()
    private val inaktivOppgave = OppgaveTestData.oppgave(aktiv = false, fristUtløpt = null)
    private val inaktivBeskjed = BeskjedTestData.beskjed(aktiv = false)
    private val inaktivInnboks = InnboksTestData.innboks(aktiv = false)


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

    @Test
    fun `Finner alle aggregerte Brukernotifikasjon-eventer fra databaseview for eventId-er`() {
        val brukernotifikasjon1 = BrukernotifikasjonObjectMother.giveMeFor(aktivBeskjed)
        val brukernotifikasjon3 = BrukernotifikasjonObjectMother.giveMeFor(aktivOppgave)
        val brukernotifikasjon2 = BrukernotifikasjonObjectMother.giveMeFor(aktivInnboks)
        val aktiveBrukernotifikasjonEventer = listOf(brukernotifikasjon1, brukernotifikasjon2, brukernotifikasjon3)
        val eventIds = aktiveBrukernotifikasjonEventer.map { it.eventId }
        runBlocking {
            val result = database.dbQuery { getBrukernotifikasjonFromViewForEventIds(eventIds) }
            result.size shouldBe 3
            result shouldContainAll aktiveBrukernotifikasjonEventer
        }
    }

}
