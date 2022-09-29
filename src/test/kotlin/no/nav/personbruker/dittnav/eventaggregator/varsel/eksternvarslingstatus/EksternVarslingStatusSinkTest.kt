package no.nav.personbruker.dittnav.eventaggregator.varsel.eksternvarslingstatus

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusRepository
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.deleteAllDoknotifikasjonStatusBeskjed
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.deleteAllDoknotifikasjonStatusInnboks
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.deleteAllDoknotifikasjonStatusOppgave
import no.nav.personbruker.dittnav.eventaggregator.innboks.deleteAllInnboks
import no.nav.personbruker.dittnav.eventaggregator.oppgave.deleteAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.varsel.BeskjedSink
import no.nav.personbruker.dittnav.eventaggregator.varsel.InnboksSink
import no.nav.personbruker.dittnav.eventaggregator.varsel.OppgaveSink
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EksternVarslingStatusSinkTest {
    private val database = LocalPostgresDatabase.migratedDb()
    private val varselRepository = VarselRepository(database)

    private val doknotifikasjonRepository = DoknotifikasjonStatusRepository(database)

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
        EksternVarslingStatusSink(
            rapidsConnection = testRapid,
            varselRepository = varselRepository,
            doknotifikasjonStatusRepository = doknotifikasjonRepository,
            writeToDb = true
        )

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
            doknotifikasjonRepository.getStatusesForBeskjed(listOf("111")).size shouldBe 1
            doknotifikasjonRepository.getStatusesForOppgave(listOf("222")).size shouldBe 1
            doknotifikasjonRepository.getStatusesForInnboks(listOf("333")).size shouldBe 1

            val eksternVarslingJsonNode = ObjectMapper().readTree(eksternVarslingStatusJson("111"))
            val status = doknotifikasjonRepository.getStatusesForBeskjed(listOf("111")).first()
            status.eventId shouldBe eksternVarslingJsonNode["eventId"].textValue()
            status.status shouldBe eksternVarslingJsonNode["status"].textValue()
            status.melding shouldBe eksternVarslingJsonNode["melding"].textValue()
            status.distribusjonsId shouldBe eksternVarslingJsonNode["distribusjonsId"].longValue()
            status.kanaler shouldBe eksternVarslingJsonNode["kanaler"].map { it.textValue() }
        }
    }

    @Test
    fun `Flere ekstern varsling-statuser oppdaterer basen`()  {
        val testRapid = TestRapid()
        EksternVarslingStatusSink(
            rapidsConnection = testRapid,
            varselRepository = varselRepository,
            doknotifikasjonStatusRepository = doknotifikasjonRepository,
            writeToDb = true
        )

        setupBeskjedSink(testRapid)
        setupOppgaveSink(testRapid)
        setupInnboksSink(testRapid)

        testRapid.sendTestMessage(varselJson("beskjed", "111"))
        testRapid.sendTestMessage(eksternVarslingStatusJson("111", status = "status1", kanaler = listOf("SMS")))
        testRapid.sendTestMessage(eksternVarslingStatusJson("111", status = "status2", kanaler = listOf("EPOST")))

        runBlocking {
            doknotifikasjonRepository.getStatusesForBeskjed(listOf("111")).size shouldBe 1

            val status = doknotifikasjonRepository.getStatusesForBeskjed(listOf("111")).first()
            status.status shouldBe "status2"
            status.kanaler shouldContainExactlyInAnyOrder setOf("EPOST", "SMS")
            status.antallOppdateringer shouldBe 2
        }
    }

    @Test
    fun `dryryn-modus når writeToDb er false`() = runBlocking {
        val testRapid = TestRapid()
        EksternVarslingStatusSink(
            rapidsConnection = testRapid,
            varselRepository = varselRepository,
            doknotifikasjonStatusRepository = doknotifikasjonRepository,
            writeToDb = false
        )

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
            doknotifikasjonRepository.getStatusesForBeskjed(listOf("111")).size shouldBe 0
            doknotifikasjonRepository.getStatusesForOppgave(listOf("222")).size shouldBe 0
            doknotifikasjonRepository.getStatusesForInnboks(listOf("333")).size shouldBe 0
        }
    }

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