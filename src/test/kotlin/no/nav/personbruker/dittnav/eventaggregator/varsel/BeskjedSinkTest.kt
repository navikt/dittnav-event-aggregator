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

        val beskjeder = getBeskjeder()
        beskjeder.size shouldBe 1
    }

    @Test
    fun `Ingorerer duplikat beskjed`() = runBlocking {
        val testRapid = TestRapid()
        BeskjedSink(testRapid, beskjedRepository)

        testRapid.sendTestMessage(beskjedJson)
        testRapid.sendTestMessage(beskjedJson)

        val beskjeder = getBeskjeder()
        beskjeder.size shouldBe 1
    }


    private suspend fun getBeskjeder(): List<Beskjed> {
        return database.dbQuery { this.prepareStatement("select * from beskjed").executeQuery().list { toBeskjed() } }
    }

    private val beskjedJson = """{
        "@event_name": "beskjed",
        "systembruker": "sb",
        "namespace": "ns",
        "appnavn": "app",
        "eventId": "395737",
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