package no.nav.personbruker.dittnav.eventaggregator.done.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.apache.kafka.clients.producer.MockProducer
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
    fun `produserer varselInaktivert event`(){
        val expectedEventId = "sghj1654"
        rapidProducer.cancelEksternVarsling(expectedEventId)
        mockProducer.history().size shouldBe 1
        mockProducer.history().first().apply {
            this.topic() shouldBe "testtopic"
            this.key() shouldBe expectedEventId
            val msg = jacksonObjectMapper().readTree(this.value())
            msg["eventId"].textValue() shouldBe expectedEventId
            msg["@event_name"].textValue() shouldBe "varselInaktivert"
        }
    }


}