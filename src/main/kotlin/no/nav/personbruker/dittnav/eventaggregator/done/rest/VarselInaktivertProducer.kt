package no.nav.personbruker.dittnav.eventaggregator.done.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.metrics.RapidMetricsProbe
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class VarselInaktivertProducer(
    private val kafkaProducer: Producer<String, String>,
    private val topicName: String,
    private val rapidMetricsProbe: RapidMetricsProbe
) {
    val log: Logger = LoggerFactory.getLogger(Producer::class.java)
    private val objectMapper = jacksonObjectMapper()

    fun cancelEksternVarsling(eventId: String) {
        val objectNode = objectMapper.createObjectNode()
        objectNode.put("@event_name", "varselInaktivert")
        objectNode.put("eventId", eventId)
        val producerRecord = ProducerRecord(topicName, eventId, objectNode.toString())
        kafkaProducer.send(producerRecord)
        runBlocking {
            rapidMetricsProbe.countVarselInaktivertProduced()
        }
    }

    fun flushAndClose() {
        try {
            kafkaProducer.flush()
            kafkaProducer.close()
            log.info("Produsent for kafka-eventer er flushet og lukket.")
        } catch (e: Exception) {
            log.warn("Klarte ikke å flushe og lukke produsent. Det kan være eventer som ikke ble produsert.")
        }
    }
}