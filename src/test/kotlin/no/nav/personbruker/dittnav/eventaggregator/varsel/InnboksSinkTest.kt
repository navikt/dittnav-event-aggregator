package no.nav.personbruker.dittnav.eventaggregator.varsel

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksRepository
import no.nav.personbruker.dittnav.eventaggregator.innboks.deleteAllInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.Innboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.toInnboks
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InnboksSinkTest {
    private val database = LocalPostgresDatabase.migratedDb()
    private val innboksRepository = InnboksRepository(database)

    @BeforeEach
    fun resetDb() {
        runBlocking {
            database.dbQuery { deleteAllInnboks() }
        }
    }

    @Test
    fun `Lagrer innboks`() = runBlocking {
        val testRapid = TestRapid()
        InnboksSink(testRapid, innboksRepository)
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
        InnboksSink(testRapid, innboksRepository)
        testRapid.sendTestMessage(innboksJson)
        testRapid.sendTestMessage(innboksJson)

        innboksFromDb().size shouldBe 1
    }

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