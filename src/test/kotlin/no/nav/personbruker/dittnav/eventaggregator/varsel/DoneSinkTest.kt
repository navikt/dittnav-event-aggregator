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
import no.nav.personbruker.dittnav.eventaggregator.done.DonePersistingService
import no.nav.personbruker.dittnav.eventaggregator.done.DoneRepository
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksRepository
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DoneSinkTest {
    private val database = LocalPostgresDatabase.migratedDb()
    private val beskjedRepository = BeskjedRepository(database)
    private val oppgaveRepository = OppgaveRepository(database)
    private val innboksRepository = InnboksRepository(database)

    private val doneRepository = DoneRepository(database)
    private val donePersistingService = DonePersistingService(doneRepository)

    @BeforeEach
    fun resetDb() {
        runBlocking {
            database.dbQuery { deleteAllBeskjed() }
        }
    }

    @Test
    fun `Inaktiverer varsel`() = runBlocking {
        val testRapid = TestRapid()
        BeskjedSink(testRapid, beskjedRepository)
        DoneSink(testRapid, doneRepository, donePersistingService)

        testRapid.sendTestMessage(varselJson("beskjed", "12345"))
        testRapid.sendTestMessage(doneJson("12345"))

        aktiveBeskjederFromDb().size shouldBe 0
    }

    private suspend fun aktiveBeskjederFromDb(): List<Beskjed> {
        return database.dbQuery {
            this.prepareStatement("select * from beskjed where aktiv = true").executeQuery().list { toBeskjed() }
        }
    }

    private fun doneJson(eventId: String) = """{
        "@event_name": "done",
         "namespace": "ns",
         "appnavn": "app",
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