package no.nav.personbruker.dittnav.eventaggregator.done

import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.personbruker.dittnav.eventaggregator.beskjed.*
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedSink
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import no.nav.personbruker.dittnav.eventaggregator.varsel.HendelseType
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselHendelse
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BeskjedInaktivertAvBrukerSinkTest {
    private val database = LocalPostgresDatabase.migratedDb()
    private val varselRepository = VarselRepository(database)

    private val varselInaktivertProducer: VarselInaktivertProducer = mockk(relaxed = true)

    @BeforeEach
    fun reset() {
        runBlocking {
            database.dbQuery { deleteAllBeskjed() }
            database.dbQuery { deleteAllDone() }
        }
        clearMocks(varselInaktivertProducer)
    }

    @Test
    fun `Inaktiverer varsel etter bruker inaktiverte beskjed i varsel-authority`() = runBlocking {
        val testRapid = TestRapid()
        setupBeskjedSink(testRapid)
        setupBeskjedInaktivertAvBrukerSink(testRapid)

        testRapid.sendTestMessage(varselJson("beskjed", "11"))
        testRapid.sendTestMessage(inaktivertIAuthorityJson("11", "bruker"))

        aktiveBeskjederFromDb().size shouldBe 0
        getBeskjedFromDb("11").fristUtløpt shouldBe false

        verify { varselInaktivertProducer.varselInaktivert(inaktivert(VarselType.BESKJED, "11"), VarselInaktivertKilde.Bruker) }
    }

    @Test
    fun `ignorerer andre årsaker til inaktivert varsel`() = runBlocking {
        val testRapid = TestRapid()
        setupBeskjedSink(testRapid)
        setupBeskjedInaktivertAvBrukerSink(testRapid)

        testRapid.sendTestMessage(varselJson("beskjed", "11"))
        testRapid.sendTestMessage(inaktivertIAuthorityJson("11", "produsent"))
        testRapid.sendTestMessage(inaktivertIAuthorityJson("11", "frist"))

        aktiveBeskjederFromDb().size shouldBe 1

        verify(exactly = 0) { varselInaktivertProducer.varselInaktivert(any(), any()) }
    }

    @Test
    fun `ignorerer egne inaktivert-eventer`() = runBlocking {
        val testRapid = TestRapid()
        setupBeskjedSink(testRapid)
        setupBeskjedInaktivertAvBrukerSink(testRapid)

        testRapid.sendTestMessage(varselJson("beskjed", "11"))
        testRapid.sendTestMessage(inaktivertIAggregatorJson("11", "bruker"))

        aktiveBeskjederFromDb().size shouldBe 1

        verify(exactly = 0) { varselInaktivertProducer.varselInaktivert(any(), any()) }
    }


    @Test
    fun `ignorerer repeterte inaktivert-varsler`() = runBlocking {
        val testRapid = TestRapid()
        setupBeskjedSink(testRapid)
        setupBeskjedInaktivertAvBrukerSink(testRapid)

        testRapid.sendTestMessage(varselJson("beskjed", "11"))
        testRapid.sendTestMessage(inaktivertIAuthorityJson("11", "bruker"))
        testRapid.sendTestMessage(inaktivertIAuthorityJson("11", "bruker"))
        testRapid.sendTestMessage(inaktivertIAuthorityJson("11", "bruker"))

        aktiveBeskjederFromDb().size shouldBe 0
        getBeskjedFromDb("11").fristUtløpt shouldBe false

        verify(exactly = 1) { varselInaktivertProducer.varselInaktivert(any(), any()) }
    }


    private fun setupBeskjedSink(testRapid: TestRapid) = BeskjedSink(
        rapidsConnection = testRapid,
        varselRepository = varselRepository,
        varselAktivertProducer = mockk(relaxed = true),
        rapidMetricsProbe = mockk(relaxed = true)
    )

    private fun setupBeskjedInaktivertAvBrukerSink(testRapid: TestRapid) = BeskjedInaktivertAvBrukerSink(
        rapidsConnection = testRapid,
        varselRepository = varselRepository,
        varselInaktivertProducer = varselInaktivertProducer
    )


    private suspend fun aktiveBeskjederFromDb(): List<Beskjed> {
        return database.dbQuery {
            this.prepareStatement("SELECT * FROM beskjed WHERE aktiv = TRUE").executeQuery().list { toBeskjed() }
        }
    }

    private fun getBeskjedFromDb(eventId: String): Beskjed = runBlocking {
        database.dbQuery { getBeskjedByEventId(eventId) }
    }


    private fun inaktivertIAuthorityJson(eventId: String, kilde: String) = """{
         "@event_name": "inaktivert",
         "@source": "varsel-authority",
         "varselId": "$eventId",
         "varselType": "beskjed",
         "namespace": "ns",
         "appnavn": "app",
         "kilde": "$kilde",
         "tidspunkt": "2023-06-25 00:00:00Z"
    }""".trimIndent()

    private fun inaktivertIAggregatorJson(eventId: String, kilde: String) = """{
         "@event_name": "inaktivert",
         "@source": "varsel-authority",
         "eventId": "$eventId",
         "varselType": "beskjed",
         "namespace": "ns",
         "appnavn": "app",
         "kilde": "$kilde",
         "tidspunkt": "2023-06-25 00:00:00"
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

    fun inaktivert(type: VarselType, eventId: String) = VarselHendelse(HendelseType.Inaktivert, type, eventId, "ns", "app")
}
