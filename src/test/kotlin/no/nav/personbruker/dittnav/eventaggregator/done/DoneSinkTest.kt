package no.nav.personbruker.dittnav.eventaggregator.done

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedSink
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedTestData
import no.nav.personbruker.dittnav.eventaggregator.beskjed.createBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.getBeskjedByEventId
import no.nav.personbruker.dittnav.eventaggregator.beskjed.toBeskjed
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import no.nav.personbruker.dittnav.eventaggregator.innboks.Innboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksSink
import no.nav.personbruker.dittnav.eventaggregator.innboks.deleteAllInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.toInnboks
import no.nav.personbruker.dittnav.eventaggregator.oppgave.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveSink
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveTestData
import no.nav.personbruker.dittnav.eventaggregator.oppgave.createOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.deleteAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.getOppgaveByEventId
import no.nav.personbruker.dittnav.eventaggregator.oppgave.toOppgave
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DoneSinkTest {
    private val database = LocalPostgresDatabase.migratedDb()
    private val varselRepository = VarselRepository(database)

    private val varselInaktivertProducer: VarselInaktivertProducer = mockk(relaxed = true)

    @BeforeEach
    fun reset() {
        runBlocking {
            database.dbQuery { deleteAllBeskjed() }
            database.dbQuery { deleteAllOppgave() }
            database.dbQuery { deleteAllInnboks() }
            database.dbQuery { deleteAllDone() }
        }
        clearMocks(varselInaktivertProducer)
    }

    @Test
    fun `Inaktiverer varsel`() = runBlocking {
        val testRapid = TestRapid()
        setupBeskjedSink(testRapid)
        OppgaveSink(testRapid, varselRepository, mockk(relaxed = true), mockk(relaxed = true))
        InnboksSink(testRapid, varselRepository, mockk(relaxed = true), mockk(relaxed = true))
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
        getBeskjedFromDb("11").fristUtløpt shouldBe false
        getOppgaveFromDb("22").fristUtløpt shouldBe false


        verify { varselInaktivertProducer.cancelEksternVarsling("11") }
        verify { varselInaktivertProducer.cancelEksternVarsling("22") }
        verify { varselInaktivertProducer.cancelEksternVarsling("33") }
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

        verify { varselInaktivertProducer.cancelEksternVarsling("11") }
        verify(exactly = 0) { varselInaktivertProducer.cancelEksternVarsling("645") }
    }

    @Test
    fun `oppdaterer ikke frist_utløpt på duplikat done`(){
        val testRapid = TestRapid()
        setupDoneSink(testRapid)

        val fristUtløpt = BeskjedTestData.beskjed(eventId = "22", fristUtløpt = true, aktiv = false)
        val fristIkkeUtløpt = BeskjedTestData.beskjed(eventId = "23", fristUtløpt = false, aktiv = false)
        val fristUtløptErNull = BeskjedTestData.beskjed(eventId = "24",fristUtløpt = null, aktiv = false)
        val fristUtløptErNullOppgave = OppgaveTestData.oppgave(eventId = "27",fristUtløpt = null, aktiv = false)
        val fristUtløptOppgave = OppgaveTestData.oppgave(eventId = "28",fristUtløpt = true, aktiv = false)

        runBlocking {
            database.dbQuery {
                createBeskjed(fristUtløpt)
                createBeskjed(fristIkkeUtløpt)
                createBeskjed(fristUtløptErNull)
                createOppgave(fristUtløptErNullOppgave)
                createOppgave(fristUtløptOppgave)
            }
        }
        testRapid.sendTestMessage(doneJson(fristUtløpt.eventId))
        testRapid.sendTestMessage(doneJson(fristIkkeUtløpt.eventId))
        testRapid.sendTestMessage(doneJson(fristUtløptErNull.eventId))

        getBeskjedFromDb(fristUtløpt.eventId).fristUtløpt shouldBe true
        getBeskjedFromDb(fristIkkeUtløpt.eventId).fristUtløpt shouldBe false
        getBeskjedFromDb(fristUtløptErNull.eventId).fristUtløpt shouldBe null

        testRapid.sendTestMessage(doneJson(fristUtløptErNullOppgave.eventId))
        testRapid.sendTestMessage(doneJson(fristUtløptOppgave.eventId))

        getOppgaveFromDb(fristUtløptOppgave.eventId).fristUtløpt shouldBe true
        getOppgaveFromDb(fristUtløptErNullOppgave.eventId).fristUtløpt shouldBe null
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

        verify(exactly = 0) { varselInaktivertProducer.cancelEksternVarsling("999") }
    }

    private fun setupBeskjedSink(testRapid: TestRapid) = BeskjedSink(
        rapidsConnection = testRapid,
        varselRepository = varselRepository,
        varselAktivertProducer = mockk(relaxed = true),
        rapidMetricsProbe = mockk(relaxed = true)
    )

    private fun setupDoneSink(testRapid: TestRapid) = DoneSink(
        rapidsConnection = testRapid,
        varselRepository = varselRepository,
        varselInaktivertProducer = varselInaktivertProducer,
        rapidMetricsProbe = mockk(relaxed = true)
    )


    private suspend fun aktiveBeskjederFromDb(): List<Beskjed> {
        return database.dbQuery {
            this.prepareStatement("SELECT * FROM beskjed WHERE aktiv = TRUE").executeQuery().list { toBeskjed() }
        }
    }

    private suspend fun aktiveOppgaverFromDb(): List<Oppgave> {
        return database.dbQuery {
            this.prepareStatement("SELECT * FROM oppgave WHERE aktiv = TRUE").executeQuery().list { toOppgave() }
        }
    }

    private suspend fun aktiveInnboksvarslerFromDb(): List<Innboks> {
        return database.dbQuery {
            this.prepareStatement("SELECT * FROM innboks WHERE aktiv = TRUE").executeQuery().list { toInnboks() }
        }
    }

    private suspend fun doneFromWaitingTable(): List<Done> {
        return database.dbQuery {
            this.prepareStatement("SELECT * FROM done").executeQuery().list { toDoneEvent() }
        }
    }

    private fun getBeskjedFromDb(eventId: String): Beskjed = runBlocking {
        database.dbQuery { getBeskjedByEventId(eventId) }
    }

    private fun getOppgaveFromDb(eventId: String): Oppgave = runBlocking {
        database.dbQuery { getOppgaveByEventId( eventId) }
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
