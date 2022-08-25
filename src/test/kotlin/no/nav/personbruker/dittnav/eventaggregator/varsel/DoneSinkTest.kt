package no.nav.personbruker.dittnav.eventaggregator.varsel

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.toBeskjed
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import no.nav.personbruker.dittnav.eventaggregator.done.Done
import no.nav.personbruker.dittnav.eventaggregator.done.DoneRepository
import no.nav.personbruker.dittnav.eventaggregator.done.deleteAllDone
import no.nav.personbruker.dittnav.eventaggregator.done.toDoneEvent
import no.nav.personbruker.dittnav.eventaggregator.innboks.Innboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksRepository
import no.nav.personbruker.dittnav.eventaggregator.innboks.deleteAllInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.toInnboks
import no.nav.personbruker.dittnav.eventaggregator.oppgave.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveRepository
import no.nav.personbruker.dittnav.eventaggregator.oppgave.deleteAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.toOppgave
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DoneSinkTest {
    private val database = LocalPostgresDatabase.migratedDb()
    private val beskjedRepository = BeskjedRepository(database)
    private val oppgaveRepository = OppgaveRepository(database)
    private val innboksRepository = InnboksRepository(database)

    private val doneRepository = DoneRepository(database)

    @BeforeEach
    fun resetDb() {
        runBlocking {
            database.dbQuery { deleteAllBeskjed() }
            database.dbQuery { deleteAllOppgave() }
            database.dbQuery { deleteAllInnboks() }
            database.dbQuery { deleteAllDone() }
        }
    }

    @Test
    fun `Inaktiverer varsel`() = runBlocking {
        val testRapid = TestRapid()
        BeskjedSink(testRapid, beskjedRepository)
        OppgaveSink(testRapid, oppgaveRepository)
        InnboksSink(testRapid, innboksRepository)
        DoneSink(testRapid, doneRepository)

        testRapid.sendTestMessage(varselJson("beskjed", "11"))
        testRapid.sendTestMessage(varselJson("oppgave", "22"))
        testRapid.sendTestMessage(varselJson("innboks", "33"))
        testRapid.sendTestMessage(doneJson("11"))
        testRapid.sendTestMessage(doneJson("22"))
        testRapid.sendTestMessage(doneJson("33"))

        aktiveBeskjederFromDb().size shouldBe 0
        aktiveOppgaverFromDb().size shouldBe 0
        aktiveInnboksvarslerFromDb().size shouldBe 0
    }

    @Test
    fun `Ignorerer duplikat done`() = runBlocking {
        val testRapid = TestRapid()
        BeskjedSink(testRapid, beskjedRepository)
        DoneSink(testRapid, doneRepository)

        testRapid.sendTestMessage(varselJson("beskjed", "11"))
        testRapid.sendTestMessage(doneJson("11"))
        testRapid.sendTestMessage(doneJson("11"))

        aktiveBeskjederFromDb().size shouldBe 0
        doneFromWaitingTable().size shouldBe 0

        testRapid.sendTestMessage(doneJson("645"))
        testRapid.sendTestMessage(doneJson("645"))
        doneFromWaitingTable().size shouldBe 1
    }

    @Test
    fun `Legger done-eventet i ventetabell hvis det kommer før varslet`() = runBlocking {
        val testRapid = TestRapid()
        BeskjedSink(testRapid, beskjedRepository)
        DoneSink(testRapid, doneRepository)

        testRapid.sendTestMessage(doneJson("999"))
        testRapid.sendTestMessage(varselJson("beskjed", "999"))

        doneFromWaitingTable().size shouldBe 1
    }

    private suspend fun aktiveBeskjederFromDb(): List<Beskjed> {
        return database.dbQuery {
            this.prepareStatement("select * from beskjed where aktiv = true").executeQuery().list { toBeskjed() }
        }
    }

    private suspend fun aktiveOppgaverFromDb(): List<Oppgave> {
        return database.dbQuery {
            this.prepareStatement("select * from oppgave where aktiv = true").executeQuery().list { toOppgave() }
        }
    }

    private suspend fun aktiveInnboksvarslerFromDb(): List<Innboks> {
        return database.dbQuery {
            this.prepareStatement("select * from innboks where aktiv = true").executeQuery().list { toInnboks() }
        }
    }

    private suspend fun doneFromWaitingTable(): List<Done> {
        return database.dbQuery {
            this.prepareStatement("select * from done").executeQuery().list { toDoneEvent() }
        }
    }

    private fun doneJson(eventId: String) = """{
        "@event_name": "done",
         "namespace": "ns",
         "appnavn": "app",
         "eventId": "$eventId",
         "forstBehandlet": "2022-02-01T00:00:00",
         "fodselsnummer": "12345678910"
    }""".trimIndent()

    private fun varselJson(type: String, eventId: String) = """{
        "@event_name": "$type",
        "namespace": "ns",
        "appnavn": "app",
        "eventId": "$eventId",
        "forstBehandlet": "2022-02-01T00:00:00",
        "fodselsnummer": "12345678910",
        "tekst": "Tekst",
        "link": "url",
        "sikkerhetsnivaa": 4,
        "aktiv": true,
        "eksternVarsling": false,
        "prefererteKanaler": ["Sneglepost", "Brevdue"]
    }""".trimIndent()
}