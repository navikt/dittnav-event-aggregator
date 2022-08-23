package no.nav.personbruker.dittnav.eventaggregator.varsel

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import no.nav.personbruker.dittnav.eventaggregator.oppgave.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveRepository
import no.nav.personbruker.dittnav.eventaggregator.oppgave.deleteAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.toOppgave
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OppgaveSinkTest {

    private val database = LocalPostgresDatabase.migratedDb()
    private val oppgaveRepository = OppgaveRepository(database)

    @BeforeEach
    fun resetDb() {
        runBlocking {
            database.dbQuery { deleteAllOppgave() }
        }
    }

    @Test
    fun `Lagrer oppgave`() = runBlocking {
        val testRapid = TestRapid()
        OppgaveSink(testRapid, oppgaveRepository)

        val eventId = "395737"
        testRapid.sendTestMessage(oppgaveJson(eventId))

        val oppgaver = oppgaverFromDb()
        oppgaver.size shouldBe 1

        val oppgave = oppgaver.first()
        oppgave.eventId shouldBe eventId
    }

    @Test
    fun `Ingorerer duplikat oppgave`() = runBlocking {
        val testRapid = TestRapid()
        OppgaveSink(testRapid, oppgaveRepository)

        testRapid.sendTestMessage(oppgaveJson())
        testRapid.sendTestMessage(oppgaveJson())

        oppgaverFromDb().size shouldBe 1
    }

    private suspend fun oppgaverFromDb(): List<Oppgave> {
        return database.dbQuery { this.prepareStatement("select * from oppgave").executeQuery().list { toOppgave() } }
    }

    private fun oppgaveJson(eventId: String = "395737") = """{
        "@event_name": "oppgave",
        "systembruker": "sb",
        "namespace": "ns",
        "appnavn": "app",
        "eventId": "$eventId",
        "eventTidspunkt": "2022-01-01T00:00:00",
        "forstBehandlet": "2022-02-01T00:00:00",
        "fodselsnummer": "12345678910",
        "grupperingsId": "123",
        "tekst": "Tekst",
        "link": "url",
        "sikkerhetsnivaa": 4,
        "synligFremTil": "2022-04-01T00:00:00",
        "aktiv": true,
        "eksternVarsling": false,
        "prefererteKanaler": ["Sneglepost", "Brevdue"]
    }""".trimIndent()
}