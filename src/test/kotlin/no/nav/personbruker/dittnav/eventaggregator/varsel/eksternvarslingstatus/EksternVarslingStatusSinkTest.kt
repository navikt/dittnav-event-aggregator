package no.nav.personbruker.dittnav.eventaggregator.varsel.eksternvarslingstatus

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusRepository
import no.nav.personbruker.dittnav.eventaggregator.varsel.BeskjedSink
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class EksternVarslingStatusSinkTest {
    private val database = LocalPostgresDatabase.migratedDb()
    private val varselRepository = VarselRepository(database)

    private val doknotifikasjonRepository = DoknotifikasjonStatusRepository(database)

    @BeforeEach
    fun resetDb() {
        runBlocking {
            database.dbQuery { deleteAllBeskjed() }
        }
    }

    @Test
    fun `Lagrer ekstern varsling-status for beskjed`() = runBlocking {
        val testRapid = TestRapid()
        EksternVarslingStatusSink(
            rapidsConnection = testRapid,
            varselRepository = varselRepository,
            doknotifikasjonStatusRepository = doknotifikasjonRepository,
            writeToDb = true
        )

        setupBeskjedSink(testRapid)

        testRapid.sendTestMessage(varselJson("beskjed", "123"))
        testRapid.sendTestMessage(eksternVarslingStatusJson("123"))

        doknotifikasjonRepository.getStatusesForBeskjed(listOf("123")).size shouldBe 1
    }

    @Test
    @Disabled
    fun `Lagrer ekstern varsling-status for oppgave`() = runBlocking {
    }

    @Test
    @Disabled
    fun `Lagrer ekstern varsling-status for innboks`() = runBlocking {
    }

    @Test
    @Disabled
    fun `Ignorerer duplikat ekstern varsling-status`() = runBlocking {

    }

    @Test
    @Disabled
    fun `dryryn-modus når writeToDb er false`() = runBlocking {
    }

    private fun setupBeskjedSink(testRapid: TestRapid, writeToDb: Boolean = true) = BeskjedSink(
        rapidsConnection = testRapid,
        varselRepository = varselRepository,
        rapidMetricsProbe = mockk(relaxed = true),
        writeToDb = writeToDb
    )

    private fun eksternVarslingStatusJson(eventId: String) = """{
        "@event_name": "eksternVarslingStatus",
        "eventId": "$eventId",
        "bestillerAppnavn": "app",
        "status": "FERDIGSTILT",
        "melding": "notifikasjon sendt via epost",
        "distribusjonsId": 12345,
        "kanaler": ["EPOST"]
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