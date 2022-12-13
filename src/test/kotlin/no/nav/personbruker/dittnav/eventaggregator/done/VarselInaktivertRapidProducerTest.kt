package no.nav.personbruker.dittnav.eventaggregator.done

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

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
    fun `produserer inaktivert og varselInaktivert event`(){
        val expectedEventId = "sghj1654"
        rapidProducer.cancelEksternVarsling(expectedEventId)
        mockProducer.history().size shouldBe 2

        val legacyEvent = mockProducer.history().find { it.valueToJson()["@event_name"].textValue() == "varselInaktivert" }
        requireNotNull(legacyEvent)
        legacyEvent.apply {
            this.topic() shouldBe "testtopic"
            this.key() shouldBe expectedEventId
            val msg = jacksonObjectMapper().readTree(this.value())
            msg["eventId"].textValue() shouldBe expectedEventId
            msg["@event_name"].textValue() shouldBe "varselInaktivert"
        }

        val inaktivertEvent = mockProducer.history().find { it.valueToJson()["@event_name"].textValue() == "inaktivert" }
        requireNotNull(inaktivertEvent)
        inaktivertEvent.apply {
            this.topic() shouldBe "testtopic"
            this.key() shouldBe expectedEventId
            val msg = jacksonObjectMapper().readTree(this.value())
            msg["eventId"].textValue() shouldBe expectedEventId
            msg["@event_name"].textValue() shouldBe "inaktivert"
        }
    }

    private fun ProducerRecord<String, String>.valueToJson() = jacksonObjectMapper().readTree(value())
}
