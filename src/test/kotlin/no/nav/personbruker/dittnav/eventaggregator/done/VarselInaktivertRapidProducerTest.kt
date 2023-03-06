package no.nav.personbruker.dittnav.eventaggregator.done

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import no.nav.personbruker.dittnav.eventaggregator.done.VarselInaktivertKilde.Produsent
import no.nav.personbruker.dittnav.eventaggregator.varsel.HendelseType.Inaktivert
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselHendelse
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType.BESKJED
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class VarselInaktivertRapidProducerTest{

    private val mockProducer = MockProducer(
        false,
         StringSerializer(),
         StringSerializer()
    )

    private val rapidProducer = VarselInaktivertProducer(kafkaProducer = mockProducer, topicName = "testtopic", mockk(relaxed = true))

    @AfterAll
    fun cleanup(){
        rapidProducer.flushAndClose()
    }

    @Test
    fun `produserer inaktivert event`(){
        val expectedEventId = "sghj1654"
        val expectedNamespace = "namespace"
        val expectedAppnavn = "appnavn"
        val hendelse = VarselHendelse(Inaktivert, BESKJED, expectedEventId, expectedNamespace, expectedAppnavn)
        rapidProducer.varselInaktivert(hendelse, Produsent)
        mockProducer.history().size shouldBe 1

        val event = mockProducer.history().find { it.valueToJson()["@event_name"].textValue() == "inaktivert" }
        requireNotNull(event)
        event.apply {
            this.topic() shouldBe "testtopic"
            this.key() shouldBe expectedEventId
            val msg = jacksonObjectMapper().readTree(this.value())
            msg["eventId"].textValue() shouldBe expectedEventId
            msg["namespace"].textValue() shouldBe expectedNamespace
            msg["appnavn"].textValue() shouldBe expectedAppnavn
            msg["@event_name"].textValue() shouldBe "inaktivert"
            msg["kilde"].textValue() shouldBe "produsent"
            msg["tidspunkt"].textValue().let { LocalDateTime.parse(it) } shouldNotBe null
        }
    }

    private fun ProducerRecord<String, String>.valueToJson() = jacksonObjectMapper().readTree(value())
}
