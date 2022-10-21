package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedSink
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksSink
import no.nav.personbruker.dittnav.eventaggregator.innboks.deleteAllInnboks
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveSink
import no.nav.personbruker.dittnav.eventaggregator.oppgave.deleteAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EksternVarslingStatusSinkTest {
    private val database = LocalPostgresDatabase.migratedDb()
    private val varselRepository = VarselRepository(database)

    private val eksternVarslingStatusRepository = EksternVarslingStatusRepository(database)
    private val eksternVarslingStatusUpdater =
        EksternVarslingStatusUpdater(eksternVarslingStatusRepository, varselRepository)

    @BeforeEach
    fun resetDb() {
        runBlocking {
            database.dbQuery { deleteAllDoknotifikasjonStatusBeskjed() }
            database.dbQuery { deleteAllDoknotifikasjonStatusOppgave() }
            database.dbQuery { deleteAllDoknotifikasjonStatusInnboks() }
            database.dbQuery { deleteAllBeskjed() }
            database.dbQuery { deleteAllOppgave() }
            database.dbQuery { deleteAllInnboks() }
        }
    }

    @Test
    fun `Lagrer ekstern varsling-status`() {
        val testRapid = TestRapid()
        setupEksternVarslingStatusSink(testRapid)
        setupBeskjedSink(testRapid)
        setupOppgaveSink(testRapid)
        setupInnboksSink(testRapid)

        testRapid.sendTestMessage(varselJson("beskjed", "111"))
        testRapid.sendTestMessage(eksternVarslingStatusJson("111"))

        testRapid.sendTestMessage(varselJson("oppgave", "222"))
        testRapid.sendTestMessage(eksternVarslingStatusJson("222"))

        testRapid.sendTestMessage(varselJson("innboks", "333"))
        testRapid.sendTestMessage(eksternVarslingStatusJson("333"))

        runBlocking {
            eksternVarslingStatusRepository.getStatusIfExists("111", VarselType.BESKJED) shouldNotBe null
            eksternVarslingStatusRepository.getStatusIfExists("222", VarselType.OPPGAVE) shouldNotBe null
            eksternVarslingStatusRepository.getStatusIfExists("333", VarselType.INNBOKS).also {
                it shouldNotBe null

                val eksternVarslingJsonNode = ObjectMapper().readTree(eksternVarslingStatusJson("333"))
                it!!.eventId shouldBe eksternVarslingJsonNode["eventId"].textValue()
                it.status shouldBe eksternVarslingJsonNode["status"].textValue()
                it.melding shouldBe eksternVarslingJsonNode["melding"].textValue()
                it.distribusjonsId shouldBe eksternVarslingJsonNode["distribusjonsId"].longValue()
                it.kanaler shouldBe eksternVarslingJsonNode["kanaler"].map { it.textValue() }
            }
        }
    }

    @Test
    fun `Flere ekstern varsling-statuser oppdaterer basen`() {
        val testRapid = TestRapid()
        setupEksternVarslingStatusSink(testRapid)
        setupBeskjedSink(testRapid)

        testRapid.sendTestMessage(varselJson("beskjed", "111"))
        testRapid.sendTestMessage(eksternVarslingStatusJson("111", status = "status1", kanaler = listOf("SMS")))
        testRapid.sendTestMessage(eksternVarslingStatusJson("111", status = "status2", kanaler = listOf("EPOST")))

        runBlocking {
            val status = eksternVarslingStatusRepository.getStatusIfExists("111", VarselType.BESKJED)
            status shouldNotBe null
            status!!.status shouldBe "status2"
            status.kanaler shouldContainExactlyInAnyOrder setOf("EPOST", "SMS")
            status.antallOppdateringer shouldBe 2
        }
    }

    @Test
    fun `dryryn-modus når writeToDb er false`() = runBlocking {
        val testRapid = TestRapid()
        setupEksternVarslingStatusSink(testRapid, writeToDb = false)
        setupBeskjedSink(testRapid)
        setupOppgaveSink(testRapid)
        setupInnboksSink(testRapid)

        testRapid.sendTestMessage(varselJson("beskjed", "111"))
        testRapid.sendTestMessage(eksternVarslingStatusJson("111"))

        testRapid.sendTestMessage(varselJson("oppgave", "222"))
        testRapid.sendTestMessage(eksternVarslingStatusJson("222"))

        testRapid.sendTestMessage(varselJson("innboks", "333"))
        testRapid.sendTestMessage(eksternVarslingStatusJson("333"))

        runBlocking {
            eksternVarslingStatusRepository.getStatusIfExists("111", VarselType.BESKJED) shouldBe null
            eksternVarslingStatusRepository.getStatusIfExists("222", VarselType.OPPGAVE) shouldBe null
            eksternVarslingStatusRepository.getStatusIfExists("333", VarselType.INNBOKS) shouldBe null
        }
    }

    @Test
    fun `gjør ingenting hvis eventId er ukjent`() {
        val testRapid = TestRapid()
        setupEksternVarslingStatusSink(testRapid)

        testRapid.sendTestMessage(eksternVarslingStatusJson("111"))
        runBlocking {
            eksternVarslingStatusRepository.getStatusIfExists("111", VarselType.BESKJED) shouldBe null
            eksternVarslingStatusRepository.getStatusIfExists("111", VarselType.OPPGAVE) shouldBe null
            eksternVarslingStatusRepository.getStatusIfExists("111", VarselType.INNBOKS) shouldBe null
        }
    }

    private fun setupEksternVarslingStatusSink(testRapid: TestRapid, writeToDb: Boolean = true) =
        EksternVarslingStatusSink(
            rapidsConnection = testRapid,
            eksternVarslingStatusUpdater = eksternVarslingStatusUpdater,
            writeToDb = writeToDb
        )

    private fun setupBeskjedSink(testRapid: TestRapid, writeToDb: Boolean = true) = BeskjedSink(
        rapidsConnection = testRapid,
        varselRepository = varselRepository,
        rapidMetricsProbe = mockk(relaxed = true),
        writeToDb = writeToDb
    )

    private fun setupOppgaveSink(testRapid: TestRapid, writeToDb: Boolean = true) = OppgaveSink(
        rapidsConnection = testRapid,
        varselRepository = varselRepository,
        rapidMetricsProbe = mockk(relaxed = true),
        writeToDb = writeToDb
    )

    private fun setupInnboksSink(testRapid: TestRapid, writeToDb: Boolean = true) = InnboksSink(
        rapidsConnection = testRapid,
        varselRepository = varselRepository,
        rapidMetricsProbe = mockk(relaxed = true),
        writeToDb = writeToDb
    )

    private fun eksternVarslingStatusJson(
        eventId: String,
        status: String = "FERDIGSTILT",
        kanaler: List<String> = listOf("EPOST")
    ) = """{
        "@event_name": "eksternVarslingStatus",
        "eventId": "$eventId",
        "status": "$status",
        "melding": "notifikasjon sendt via epost",
        "distribusjonsId": 12345,
        "kanaler": ${kanaler.joinToString("\", \"", "[\"", "\"]")}
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