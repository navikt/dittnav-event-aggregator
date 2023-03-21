package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class EksternVarslingOppdatertProducer(private val kafkaProducer: Producer<String, String>,
                                       private val topicName: String
) {
    val log: Logger = LoggerFactory.getLogger(Producer::class.java)
    private val objectMapper = jacksonObjectMapper()

    fun eksternStatusOppdatert(oppdatering: EksternStatusOppdatering) {

        val objectNode = objectMapper.createObjectNode()
        objectNode.put("@event_name", "eksternStatusOppdatert")
        objectNode.put("status", oppdatering.status.lowercaseName)
        objectNode.put("eventId", oppdatering.eventId)
        objectNode.put("varselType", oppdatering.varselType.eventType)
        objectNode.put("namespace", oppdatering.namespace)
        objectNode.put("appnavn", oppdatering.appnavn)
        objectNode.put("tidspunkt", nowAtUtc().toString())

        if (oppdatering.status == EksternStatus.Sendt) {
            objectNode.put("kanal", oppdatering.kanal)
            objectNode.put("renotifikasjon", oppdatering.renotifikasjon)
        }

        val producerRecord = ProducerRecord(topicName, oppdatering.eventId, objectNode.toString())

        kafkaProducer.send(producerRecord)
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
