package no.nav.personbruker.dittnav.eventaggregator.varsel

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedTestData
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksTestData
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveTestData
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType.*
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class VarselAktivertProducerTest {

    private val objectMapper = jacksonObjectMapper()

    private val mockProducer = MockProducer(
        false,
        StringSerializer(),
        StringSerializer()
    )
    private val rapidProducer = VarselAktivertProducer(mockProducer, "testtopic", mockk(relaxed = true))

    @AfterEach
    fun cleanup() {
        mockProducer.clear()
    }

    @Test
    fun `sender varsel-aktivert event for beskjed`() {

        val beskjed = BeskjedTestData.beskjed()

        rapidProducer.varselAktivert(beskjed)

        val outputJson = mockProducer.history()
            .first()
            .value()
            .let { objectMapper.readTree(it) }

        outputJson["@event_name"].textValue() shouldBe "aktivert"
        outputJson["varselType"].textValue() shouldBe BESKJED.eventType
        outputJson["systembruker"].textValue() shouldBe beskjed.systembruker
        outputJson["namespace"].textValue() shouldBe beskjed.namespace
        outputJson["appnavn"].textValue() shouldBe beskjed.appnavn
        outputJson["eventId"].textValue() shouldBe beskjed.eventId
        outputJson["eventTidspunkt"].asLocalDateTime() shouldBe beskjed.eventTidspunkt
        outputJson["forstBehandlet"].asLocalDateTime() shouldBe beskjed.forstBehandlet
        outputJson["fodselsnummer"].textValue() shouldBe beskjed.fodselsnummer
        outputJson["grupperingsId"].textValue() shouldBe beskjed.grupperingsId
        outputJson["tekst"].textValue() shouldBe beskjed.tekst
        outputJson["link"].textValue() shouldBe beskjed.link
        outputJson["sikkerhetsnivaa"].intValue() shouldBe beskjed.sikkerhetsnivaa
        outputJson["sistOppdatert"].asLocalDateTime() shouldBe beskjed.sistOppdatert
        outputJson["synligFremTil"].asLocalDateTime() shouldBe beskjed.synligFremTil
        outputJson["aktiv"].booleanValue() shouldBe beskjed.aktiv
        outputJson["eksternVarsling"].booleanValue() shouldBe beskjed.eksternVarsling
        outputJson["prefererteKanaler"].map { it.textValue() } shouldBe beskjed.prefererteKanaler
        outputJson["smsVarslingstekst"].textValue() shouldBe beskjed.smsVarslingstekst
        outputJson["epostVarslingstekst"].textValue() shouldBe beskjed.epostVarslingstekst
        outputJson["epostVarslingstittel"].textValue() shouldBe beskjed.epostVarslingstittel
    }

    @Test
    fun `sender varsel-aktivert event for oppgave`() {

        val oppgave = OppgaveTestData.oppgave()

        rapidProducer.varselAktivert(oppgave)

        val outputJson = mockProducer.history()
            .first()
            .value()
            .let { objectMapper.readTree(it) }

        outputJson["@event_name"].textValue() shouldBe "aktivert"
        outputJson["varselType"].textValue() shouldBe OPPGAVE.eventType
        outputJson["systembruker"].textValue() shouldBe oppgave.systembruker
        outputJson["namespace"].textValue() shouldBe oppgave.namespace
        outputJson["appnavn"].textValue() shouldBe oppgave.appnavn
        outputJson["eventId"].textValue() shouldBe oppgave.eventId
        outputJson["eventTidspunkt"].asLocalDateTime() shouldBe oppgave.eventTidspunkt
        outputJson["forstBehandlet"].asLocalDateTime() shouldBe oppgave.forstBehandlet
        outputJson["fodselsnummer"].textValue() shouldBe oppgave.fodselsnummer
        outputJson["grupperingsId"].textValue() shouldBe oppgave.grupperingsId
        outputJson["tekst"].textValue() shouldBe oppgave.tekst
        outputJson["link"].textValue() shouldBe oppgave.link
        outputJson["sikkerhetsnivaa"].intValue() shouldBe oppgave.sikkerhetsnivaa
        outputJson["sistOppdatert"].asLocalDateTime() shouldBe oppgave.sistOppdatert
        outputJson["synligFremTil"].asLocalDateTime() shouldBe oppgave.synligFremTil
        outputJson["aktiv"].booleanValue() shouldBe oppgave.aktiv
        outputJson["eksternVarsling"].booleanValue() shouldBe oppgave.eksternVarsling
        outputJson["prefererteKanaler"].map { it.textValue() } shouldBe oppgave.prefererteKanaler
        outputJson["smsVarslingstekst"].textValue() shouldBe oppgave.smsVarslingstekst
        outputJson["epostVarslingstekst"].textValue() shouldBe oppgave.epostVarslingstekst
        outputJson["epostVarslingstittel"].textValue() shouldBe oppgave.epostVarslingstittel
    }

    @Test
    fun `sender varsel-aktivert event for innboks`() {

        val innboks = InnboksTestData.innboks()

        rapidProducer.varselAktivert(innboks)

        val outputJson = mockProducer.history()
            .first()
            .value()
            .let { objectMapper.readTree(it) }

        outputJson["@event_name"].textValue() shouldBe "aktivert"
        outputJson["varselType"].textValue() shouldBe INNBOKS.eventType
        outputJson["systembruker"].textValue() shouldBe innboks.systembruker
        outputJson["namespace"].textValue() shouldBe innboks.namespace
        outputJson["appnavn"].textValue() shouldBe innboks.appnavn
        outputJson["eventId"].textValue() shouldBe innboks.eventId
        outputJson["eventTidspunkt"].asLocalDateTime() shouldBe innboks.eventTidspunkt
        outputJson["forstBehandlet"].asLocalDateTime() shouldBe innboks.forstBehandlet
        outputJson["fodselsnummer"].textValue() shouldBe innboks.fodselsnummer
        outputJson["grupperingsId"].textValue() shouldBe innboks.grupperingsId
        outputJson["tekst"].textValue() shouldBe innboks.tekst
        outputJson["link"].textValue() shouldBe innboks.link
        outputJson["sikkerhetsnivaa"].intValue() shouldBe innboks.sikkerhetsnivaa
        outputJson["sistOppdatert"].asLocalDateTime() shouldBe innboks.sistOppdatert
        outputJson["aktiv"].booleanValue() shouldBe innboks.aktiv
        outputJson["eksternVarsling"].booleanValue() shouldBe innboks.eksternVarsling
        outputJson["prefererteKanaler"].map { it.textValue() } shouldBe innboks.prefererteKanaler
        outputJson["smsVarslingstekst"].textValue() shouldBe innboks.smsVarslingstekst
        outputJson["epostVarslingstekst"].textValue() shouldBe innboks.epostVarslingstekst
        outputJson["epostVarslingstittel"].textValue() shouldBe innboks.epostVarslingstittel
    }
}
