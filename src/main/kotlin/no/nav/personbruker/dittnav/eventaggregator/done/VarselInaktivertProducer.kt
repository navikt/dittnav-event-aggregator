package no.nav.personbruker.dittnav.eventaggregator.done

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.metrics.RapidMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.varsel.HendelseType.Inaktivert
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselHendelse
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger

class VarselInaktivertProducer(
    private val kafkaProducer: Producer<String, String>,
    private val topicName: String,
    private val rapidMetricsProbe: RapidMetricsProbe
) {
    val log: Logger = KotlinLogging.logger { }
    private val objectMapper = jacksonObjectMapper()

    fun varselInaktivert(hendelse: VarselHendelse, kilde: VarselInaktivertKilde) {

        val objectNode = objectMapper.createObjectNode()
        objectNode.put("@event_name", Inaktivert.lowerCaseName)
        objectNode.put("eventId", hendelse.eventId)
        objectNode.put("varselType", hendelse.varselType.eventType)
        objectNode.put("namespace", hendelse.namespace)
        objectNode.put("appnavn", hendelse.appnavn)
        objectNode.put("kilde", kilde.lowercaseName)
        objectNode.put("tidspunkt", nowAtUtc().toString())
        val producerRecord = ProducerRecord(topicName, hendelse.eventId, objectNode.toString())

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
