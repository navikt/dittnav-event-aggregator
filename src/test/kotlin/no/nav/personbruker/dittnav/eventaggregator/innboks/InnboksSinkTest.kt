package no.nav.personbruker.dittnav.eventaggregator.innboks

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InnboksSinkTest {
    private val database = LocalPostgresDatabase.migratedDb()
    private val varselRepository = VarselRepository(database)


    @BeforeEach
    fun resetDb() {
        runBlocking {
            database.dbQuery { deleteAllInnboks() }
        }
    }

    @Test
    fun `Lagrer innboks`() = runBlocking {
        val testRapid = TestRapid()
        setupInnboksSink(testRapid)
        testRapid.sendTestMessage(innboksJson)

        val innboksList = innboksFromDb()
        innboksList.size shouldBe 1

        val innboks = innboksList.first()
        val innboksJsonNode = ObjectMapper().readTree(innboksJson)
        innboks.namespace shouldBe innboksJsonNode["namespace"].textValue()
        innboks.appnavn shouldBe innboksJsonNode["appnavn"].textValue()
        innboks.eventId shouldBe innboksJsonNode["eventId"].textValue()
        innboks.forstBehandlet shouldBe innboksJsonNode["forstBehandlet"].asLocalDateTime()
        innboks.fodselsnummer shouldBe innboksJsonNode["fodselsnummer"].textValue()
        innboks.tekst shouldBe innboksJsonNode["tekst"].textValue()
        innboks.link shouldBe innboksJsonNode["link"].textValue()
        innboks.sikkerhetsnivaa shouldBe innboksJsonNode["sikkerhetsnivaa"].intValue()
        innboks.aktiv shouldBe innboksJsonNode["aktiv"].booleanValue()
        innboks.eksternVarsling shouldBe innboksJsonNode["eksternVarsling"].booleanValue()
        innboks.prefererteKanaler shouldBe innboksJsonNode["prefererteKanaler"].map { it.textValue() }
    }

    @Test
    fun `Ignorerer duplikat innboks`() = runBlocking {
        val testRapid = TestRapid()
        setupInnboksSink(testRapid)
        testRapid.sendTestMessage(innboksJson)
        testRapid.sendTestMessage(innboksJson)

        innboksFromDb().size shouldBe 1
    }

    private fun setupInnboksSink(testRapid: TestRapid) = InnboksSink(
        rapidsConnection = testRapid,
        varselRepository = varselRepository,
        rapidMetricsProbe = mockk(relaxed = true)
    )


    private suspend fun innboksFromDb(): List<Innboks> {
        return database.dbQuery { this.prepareStatement("select * from innboks").executeQuery().list { toInnboks() } }
    }

    private val innboksJson = """{
        "@event_name": "innboks",
        "namespace": "ns",
        "appnavn": "app",
        "eventId": "258237",
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