package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedSink
import no.nav.personbruker.dittnav.eventaggregator.beskjed.deleteAllBeskjed
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowAtUtcTruncated
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksSink
import no.nav.personbruker.dittnav.eventaggregator.innboks.deleteAllInnboks
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveSink
import no.nav.personbruker.dittnav.eventaggregator.oppgave.deleteAllOppgave
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType.OPPGAVE
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class EksternVarslingStatusSinkTest {
    private val database = LocalPostgresDatabase.migratedDb()
    private val varselRepository = VarselRepository(database)

    private val mockProducer = MockProducer(
        false,
        StringSerializer(),
        StringSerializer()
    )

    private val eksternVarslingOppdatertProducer = EksternVarslingOppdatertProducer(mockProducer, "testtopic")
    private val eksternVarslingStatusRepository = EksternVarslingStatusRepository(database)
    private val eksternVarslingStatusUpdater =
        EksternVarslingStatusUpdater(eksternVarslingStatusRepository, varselRepository, eksternVarslingOppdatertProducer)

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
        mockProducer.clear()
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
            eksternVarslingStatusRepository.getStatusIfExists("222", OPPGAVE) shouldNotBe null
            eksternVarslingStatusRepository.getStatusIfExists("333", VarselType.INNBOKS).also {
                it shouldNotBe null

                val eksternVarslingJsonNode = ObjectMapper().readTree(eksternVarslingStatusJson("333"))
                it!!.eventId shouldBe eksternVarslingJsonNode["eventId"].textValue()
                it.sistMottattStatus shouldBe eksternVarslingJsonNode["status"].textValue()
                it.historikk.size shouldBe 1
                it.historikk.first().melding shouldBe eksternVarslingJsonNode["melding"].textValue()
                it.historikk.first().distribusjonsId shouldBe eksternVarslingJsonNode["distribusjonsId"].longValue()
                it.kanaler shouldContain eksternVarslingJsonNode["kanal"].textValue()
            }
        }

        mockProducer.verifyOutput { output ->
            output.size shouldBe 3
        }
    }

    @Test
    fun `Flere ekstern varsling-statuser oppdaterer basen`() {
        val testRapid = TestRapid()
        setupEksternVarslingStatusSink(testRapid)
        setupBeskjedSink(testRapid)

        testRapid.sendTestMessage(varselJson("beskjed", "111"))
        testRapid.sendTestMessage(eksternVarslingStatusJson("111", status = "INFO", kanal = null))
        testRapid.sendTestMessage(eksternVarslingStatusJson("111", status = "FERDIGSTILT", kanal = "EPOST"))
        testRapid.sendTestMessage(eksternVarslingStatusJson("111", status = "FERDIGSTILT", kanal = "SMS"))

        runBlocking {
            val status = eksternVarslingStatusRepository.getStatusIfExists("111", VarselType.BESKJED)
            status shouldNotBe null
            status!!.sistMottattStatus shouldBe "FERDIGSTILT"
            status.kanaler shouldContainExactlyInAnyOrder setOf("EPOST", "SMS")
        }

        mockProducer.verifyOutput { output ->
            output.size shouldBe 3
        }
    }

    @Test
    fun `gjør ingenting hvis eventId er ukjent`() {
        val testRapid = TestRapid()
        setupEksternVarslingStatusSink(testRapid)

        testRapid.sendTestMessage(eksternVarslingStatusJson("111"))
        runBlocking {
            eksternVarslingStatusRepository.getStatusIfExists("111", VarselType.BESKJED) shouldBe null
            eksternVarslingStatusRepository.getStatusIfExists("111", OPPGAVE) shouldBe null
            eksternVarslingStatusRepository.getStatusIfExists("111", VarselType.INNBOKS) shouldBe null
        }

        mockProducer.verifyOutput { output ->
            output.size shouldBe 0
        }
    }

    @Test
    fun `takler at distribusjonsId er null`() {
        val testRapid = TestRapid()
        setupEksternVarslingStatusSink(testRapid)
        setupBeskjedSink(testRapid)

        testRapid.sendTestMessage(varselJson("beskjed", "111"))
        testRapid.sendTestMessage(eksternVarslingStatusJson("111", distribusjonsId = null))

        runBlocking {
            val status = eksternVarslingStatusRepository.getStatusIfExists("111", VarselType.BESKJED)
            status shouldNotBe null
            status!!.historikk.first().distribusjonsId shouldBe null
        }

        mockProducer.verifyOutput { output ->
            output.size shouldBe 1
        }
    }

    @Test
    fun `varsler om oppdaterte varsler`() {
        val testRapid = TestRapid()
        setupEksternVarslingStatusSink(testRapid)
        setupBeskjedSink(testRapid)

        testRapid.sendTestMessage(varselJson("beskjed", "111"))
        testRapid.sendTestMessage(eksternVarslingStatusJson("111", "OVERSENDT"))
        testRapid.sendTestMessage(eksternVarslingStatusJson("111", "INFO"))
        testRapid.sendTestMessage(eksternVarslingStatusJson("111", "FEILET"))
        testRapid.sendTestMessage(eksternVarslingStatusJson("111", "FERDIGSTILT", kanal = "SMS"))
        testRapid.sendTestMessage(eksternVarslingStatusJson("111", "FERDIGSTILT", kanal = null))

        mockProducer.verifyOutput { output ->
            output.find { it["status"].textValue() == "bestilt" } shouldNotBe null
            output.find { it["status"].textValue() == "info" } shouldNotBe null
            output.find { it["status"].textValue() == "feilet" } shouldNotBe null
            output.find { it["status"].textValue() == "sendt" } shouldNotBe null
            output.find { it["status"].textValue() == "ferdigstilt" } shouldNotBe null

            val ferdigstilt = output.find { it["status"].textValue() == "sendt" }!!
            ferdigstilt["@event_name"].textValue() shouldBe "eksternStatusOppdatert"
            ferdigstilt["eventId"].textValue() shouldBe "111"
            ferdigstilt["kanal"].textValue() shouldBe "SMS"
            ferdigstilt["renotifikasjon"].booleanValue() shouldBe false
            ferdigstilt["tidspunkt"].textValue().let { LocalDateTime.parse(it) } shouldNotBe null
        }
    }

    @Test
    fun `sjekker om status kommer fra renotifikasjon`() {
        val testRapid = TestRapid()
        setupEksternVarslingStatusSink(testRapid)
        setupOppgaveSink(testRapid)

        testRapid.sendTestMessage(varselJson("oppgave", "111"))
        testRapid.sendTestMessage(eksternVarslingStatusJson("111", "OVERSENDT", tidspunkt = nowAtUtcTruncated()))
        testRapid.sendTestMessage(eksternVarslingStatusJson("111", "FERDIGSTILT", kanal = "SMS", tidspunkt = nowAtUtcTruncated().plusDays(1)))

        testRapid.sendTestMessage(varselJson("oppgave", "222"))
        testRapid.sendTestMessage(eksternVarslingStatusJson("222", "OVERSENDT", tidspunkt = nowAtUtcTruncated()))
        testRapid.sendTestMessage(eksternVarslingStatusJson("222", "FERDIGSTILT", kanal = "SMS", tidspunkt = nowAtUtcTruncated()))
        testRapid.sendTestMessage(eksternVarslingStatusJson("222", "FERDIGSTILT", kanal = "SMS", tidspunkt = nowAtUtcTruncated().plusDays(1)))
        testRapid.sendTestMessage(eksternVarslingStatusJson("222", "FERDIGSTILT", kanal = "EPOST", tidspunkt = nowAtUtcTruncated().plusDays(1)))

        testRapid.sendTestMessage(varselJson("oppgave", "333"))
        testRapid.sendTestMessage(eksternVarslingStatusJson("333", "OVERSENDT", tidspunkt = nowAtUtcTruncated()))
        testRapid.sendTestMessage(eksternVarslingStatusJson("333", "FEILET", tidspunkt = nowAtUtcTruncated()))
        testRapid.sendTestMessage(eksternVarslingStatusJson("333", "FERDIGSTILT", kanal = "SMS", tidspunkt = nowAtUtcTruncated().plusDays(1)))

        testRapid.sendTestMessage(varselJson("oppgave", "444"))
        testRapid.sendTestMessage(eksternVarslingStatusJson("444", "OVERSENDT", tidspunkt = nowAtUtcTruncated()))
        testRapid.sendTestMessage(eksternVarslingStatusJson("444", "FEILET", tidspunkt = nowAtUtcTruncated()))
        testRapid.sendTestMessage(eksternVarslingStatusJson("444", "INFO", tidspunkt = nowAtUtcTruncated().plusDays(1)))

        runBlocking {
            val status1 = eksternVarslingStatusRepository.getStatusIfExists("111", OPPGAVE)
            status1!!.eksternVarslingSendt shouldBe true
            status1.renotifikasjonSendt shouldBe false

            val status2 = eksternVarslingStatusRepository.getStatusIfExists("222", OPPGAVE)
            status2!!.eksternVarslingSendt shouldBe true
            status2.renotifikasjonSendt shouldBe true

            val status3 = eksternVarslingStatusRepository.getStatusIfExists("333", OPPGAVE)
            status3!!.eksternVarslingSendt shouldBe true
            status3.renotifikasjonSendt shouldBe true

            val status4 = eksternVarslingStatusRepository.getStatusIfExists("444", OPPGAVE)
            status4!!.eksternVarslingSendt shouldBe false
            status4.renotifikasjonSendt shouldBe false
        }

        mockProducer.verifyOutput { output ->
            output.filter {
                it["status"].textValue() == "sendt" && it["renotifikasjon"].asBoolean()
            }.size shouldBe 3

            output.filter {
                it["status"].textValue() == "sendt" && it["renotifikasjon"].asBoolean().not()
            }.size shouldBe 2

            output.filter {
                it["renotifikasjon"] == null
            }.size shouldBe 7
        }
    }

    private fun setupEksternVarslingStatusSink(testRapid: TestRapid) =
        EksternVarslingStatusSink(
            rapidsConnection = testRapid,
            eksternVarslingStatusUpdater = eksternVarslingStatusUpdater,
        )

    private fun setupBeskjedSink(testRapid: TestRapid) = BeskjedSink(
        rapidsConnection = testRapid,
        varselRepository = varselRepository,
        varselAktivertProducer = mockk(relaxed = true),
        rapidMetricsProbe = mockk(relaxed = true)
    )

    private fun setupOppgaveSink(testRapid: TestRapid) = OppgaveSink(
        rapidsConnection = testRapid,
        varselRepository = varselRepository,
        varselAktivertProducer = mockk(relaxed = true),
        rapidMetricsProbe = mockk(relaxed = true)
    )

    private fun setupInnboksSink(testRapid: TestRapid) = InnboksSink(
        rapidsConnection = testRapid,
        varselRepository = varselRepository,
        varselAktivertProducer = mockk(relaxed = true),
        rapidMetricsProbe = mockk(relaxed = true)
    )

    private fun eksternVarslingStatusJson(
        eventId: String,
        status: String = "FERDIGSTILT",
        kanal: String? = "EPOST",
        bestiller: String = "appnavn",
        distribusjonsId: Long? = 123L,
        tidspunkt: LocalDateTime = LocalDateTimeTestHelper.nowAtUtcTruncated()
    ) = """{
        "@event_name": "eksternVarslingStatus",
        "eventId": "$eventId",
        "status": "$status",
        "melding": "notifikasjon sendt via epost",
        "distribusjonsId": $distribusjonsId,
        "bestillerAppnavn": "$bestiller",
        "kanal": ${ if(kanal == null) "null" else "\"$kanal\"" },
        "tidspunkt": "$tidspunkt"
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

private fun MockProducer<String, String>.verifyOutput(verifier: (List<JsonNode>) -> Unit) {
    val objectMapper = ObjectMapper()
    history().map { it.value() }.map { objectMapper.readTree(it) }.let(verifier)
}
