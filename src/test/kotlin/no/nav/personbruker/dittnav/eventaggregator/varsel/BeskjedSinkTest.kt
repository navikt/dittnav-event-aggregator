package no.nav.personbruker.dittnav.eventaggregator.varsel

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.toBeskjed
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BeskjedSinkTest {
    private val database = LocalPostgresDatabase.migratedDb()
    private val beskjedRepository = BeskjedRepository(database)

    @BeforeEach
    fun resetDb() {
        runBlocking {
            database.dbQuery { deleteAllBeskjed() }
        }
    }

    @Test
    fun `Lagrer beskjed`() = runBlocking {
        val testRapid = TestRapid()
        BeskjedSink(testRapid, beskjedRepository)

        testRapid.sendTestMessage(beskjedJson)

        val beskjeder = beskjederFromDb()
        beskjeder.size shouldBe 1

        val beskjed = beskjeder.first()
        val beskjedJsonNode = ObjectMapper().readTree(beskjedJson)
        beskjed.namespace shouldBe beskjedJsonNode["namespace"].textValue()
        beskjed.appnavn shouldBe beskjedJsonNode["appnavn"].textValue()
        beskjed.eventId shouldBe beskjedJsonNode["eventId"].textValue()
        beskjed.forstBehandlet shouldBe beskjedJsonNode["forstBehandlet"].asLocalDateTime()
        beskjed.fodselsnummer shouldBe beskjedJsonNode["fodselsnummer"].textValue()
        beskjed.tekst shouldBe beskjedJsonNode["tekst"].textValue()
        beskjed.link shouldBe beskjedJsonNode["link"].textValue()
        beskjed.sikkerhetsnivaa shouldBe beskjedJsonNode["sikkerhetsnivaa"].intValue()
        beskjed.synligFremTil shouldBe beskjedJsonNode["synligFremTil"].asLocalDateTime()
        beskjed.aktiv shouldBe beskjedJsonNode["aktiv"].booleanValue()
        beskjed.eksternVarsling shouldBe beskjedJsonNode["eksternVarsling"].booleanValue()
        beskjed.prefererteKanaler shouldBe beskjedJsonNode["prefererteKanaler"].map { it.textValue() }
    }

    @Test
    fun `Ignorerer duplikat beskjed`() = runBlocking {
        val testRapid = TestRapid()
        BeskjedSink(testRapid, beskjedRepository)

        testRapid.sendTestMessage(beskjedJson)
        testRapid.sendTestMessage(beskjedJson)

        val beskjeder = beskjederFromDb()
        beskjeder.size shouldBe 1
    }

    private suspend fun beskjederFromDb(): List<Beskjed> {
        return database.dbQuery { this.prepareStatement("select * from beskjed").executeQuery().list { toBeskjed() } }
    }

    private val beskjedJson = """{
        "@event_name": "beskjed",
        "namespace": "ns",
        "appnavn": "app",
        "eventId": "395737",
        "forstBehandlet": "2022-02-01T00:00:00",
        "fodselsnummer": "12345678910",
        "tekst": "Tekst",
        "link": "url",
        "sikkerhetsnivaa": 4,
        "synligFremTil": "2022-04-01T00:00:00",
        "aktiv": true,
        "eksternVarsling": false,
        "prefererteKanaler": ["Sneglepost", "Brevdue"]
    }""".trimIndent()
}