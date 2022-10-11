package no.nav.personbruker.dittnav.eventaggregator.varsel

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedSink
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.toBeskjed
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import no.nav.personbruker.dittnav.eventaggregator.done.Done
import no.nav.personbruker.dittnav.eventaggregator.done.DoneSink
import no.nav.personbruker.dittnav.eventaggregator.done.deleteAllDone
import no.nav.personbruker.dittnav.eventaggregator.done.toDoneEvent
import no.nav.personbruker.dittnav.eventaggregator.innboks.Innboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksSink
import no.nav.personbruker.dittnav.eventaggregator.innboks.deleteAllInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.toInnboks
import no.nav.personbruker.dittnav.eventaggregator.oppgave.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveSink
import no.nav.personbruker.dittnav.eventaggregator.oppgave.deleteAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.toOppgave
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DoneSinkTest {
    private val database = LocalPostgresDatabase.migratedDb()
    private val varselRepository = VarselRepository(database)

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
        setupBeskjedSink(testRapid)
        OppgaveSink(testRapid, varselRepository, mockk(relaxed = true), writeToDb = true)
        InnboksSink(testRapid, varselRepository, mockk(relaxed = true), writeToDb = true)
        setupDoneSink(testRapid)

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
        setupBeskjedSink(testRapid)
        setupDoneSink(testRapid)

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
        setupBeskjedSink(testRapid)
        setupDoneSink(testRapid)

        val doneJson = doneJson("999")
        testRapid.sendTestMessage(doneJson)
        testRapid.sendTestMessage(varselJson("beskjed", "999"))

        val doneEventer = doneFromWaitingTable()
        doneEventer.size shouldBe 1

        val done = doneEventer.first()
        val doneJsonNode = ObjectMapper().readTree(doneJson)
        done.namespace shouldBe "N/A"
        done.appnavn shouldBe "N/A"
        done.eventId shouldBe doneJsonNode["eventId"].textValue()
        done.forstBehandlet shouldBe doneJsonNode["forstBehandlet"].asLocalDateTime()
        done.fodselsnummer shouldBe doneJsonNode["fodselsnummer"].textValue()
    }

    @Test
    fun `dryryn-modus når writeToDb er false`() = runBlocking {
        val testRapid = TestRapid()
        setupDoneSink(testRapid, writeToDb = false)

        testRapid.sendTestMessage(doneJson("999"))

        val beskjeder = doneFromWaitingTable()
        beskjeder.size shouldBe 0
    }

    private fun setupBeskjedSink(testRapid: TestRapid) = BeskjedSink(
        rapidsConnection = testRapid,
        varselRepository = varselRepository,
        rapidMetricsProbe = mockk(relaxed = true),
        writeToDb = true
    )

    private fun setupDoneSink(testRapid: TestRapid, writeToDb: Boolean = true) = DoneSink(
        rapidsConnection = testRapid,
        varselRepository = varselRepository,
        rapidMetricsProbe = mockk(relaxed = true),
        writeToDb = writeToDb
    )


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