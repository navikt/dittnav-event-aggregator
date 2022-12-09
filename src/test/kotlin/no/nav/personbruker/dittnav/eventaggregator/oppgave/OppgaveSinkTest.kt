package no.nav.personbruker.dittnav.eventaggregator.oppgave

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselAktivertProducer
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OppgaveSinkTest {
    private val database = LocalPostgresDatabase.migratedDb()
    private val varselRepository = VarselRepository(database)

    private val varselAktivertProducer: VarselAktivertProducer = mockk(relaxed = true)

    @BeforeEach
    fun reset() {
        runBlocking {
            database.dbQuery { deleteAllOppgave() }
        }
        clearMocks(varselAktivertProducer)
    }

    @Test
    fun `Lagrer oppgave`() = runBlocking {
        val testRapid = TestRapid()
        setupOppgaveSink(testRapid)
        testRapid.sendTestMessage(oppgaveJson)

        val oppgaver = oppgaverFromDb()
        oppgaver.size shouldBe 1

        val oppgave = oppgaver.first()
        val oppgaveJsonNode = ObjectMapper().readTree(oppgaveJson)
        oppgave.namespace shouldBe oppgaveJsonNode["namespace"].textValue()
        oppgave.appnavn shouldBe oppgaveJsonNode["appnavn"].textValue()
        oppgave.eventId shouldBe oppgaveJsonNode["eventId"].textValue()
        oppgave.forstBehandlet shouldBe oppgaveJsonNode["forstBehandlet"].asLocalDateTime()
        oppgave.fodselsnummer shouldBe oppgaveJsonNode["fodselsnummer"].textValue()
        oppgave.tekst shouldBe oppgaveJsonNode["tekst"].textValue()
        oppgave.link shouldBe oppgaveJsonNode["link"].textValue()
        oppgave.sikkerhetsnivaa shouldBe oppgaveJsonNode["sikkerhetsnivaa"].intValue()
        oppgave.synligFremTil shouldBe oppgaveJsonNode["synligFremTil"].asLocalDateTime()
        oppgave.aktiv shouldBe oppgaveJsonNode["aktiv"].booleanValue()
        oppgave.eksternVarsling shouldBe oppgaveJsonNode["eksternVarsling"].booleanValue()
        oppgave.prefererteKanaler shouldBe oppgaveJsonNode["prefererteKanaler"].map { it.textValue() }

        verify(exactly = 1) { varselAktivertProducer.varselAktivert(any<Oppgave>()) }
    }

    @Test
    fun `Ignorerer duplikat oppgave`() = runBlocking {
        val testRapid = TestRapid()
        setupOppgaveSink(testRapid)
        testRapid.sendTestMessage(oppgaveJson)
        testRapid.sendTestMessage(oppgaveJson)

        oppgaverFromDb().size shouldBe 1
    }

    private fun setupOppgaveSink(testRapid: TestRapid) = OppgaveSink(
        rapidsConnection = testRapid,
        varselRepository = varselRepository,
        varselAktivertProducer = varselAktivertProducer,
        rapidMetricsProbe = mockk(relaxed = true)
    )


    private suspend fun oppgaverFromDb(): List<Oppgave> {
        return database.dbQuery { this.prepareStatement("select * from oppgave").executeQuery().list { toOppgave() } }
    }

    private val oppgaveJson = """{
        "@event_name": "oppgave",
        "namespace": "ns",
        "appnavn": "app",
        "eventId": "258237",
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
