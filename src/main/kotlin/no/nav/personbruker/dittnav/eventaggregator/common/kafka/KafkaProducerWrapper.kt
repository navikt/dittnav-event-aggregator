package no.nav.personbruker.dittnav.eventaggregator.common.kafka

import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory

class KafkaProducerWrapper<T>(
        val topicName: String,
        val kafkaProducer: Producer<NokkelInput, T>
) {

    private val log = LoggerFactory.getLogger(KafkaProducerWrapper::class.java)

    fun sendEvent(key: NokkelInput, event: T) {
        ProducerRecord(topicName, key, event).let { producerRecord ->
            kafkaProducer.send(producerRecord)
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
