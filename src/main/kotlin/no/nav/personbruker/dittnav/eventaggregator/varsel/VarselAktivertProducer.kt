package no.nav.personbruker.dittnav.eventaggregator.varsel

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.innboks.Innboks
import no.nav.personbruker.dittnav.eventaggregator.metrics.RapidMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.oppgave.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.varsel.HendelseType.Aktivert
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType.*
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class VarselAktivertProducer(
    private val kafkaProducer: Producer<String, String>,
    private val topicName: String,
    private val rapidMetricsProbe: RapidMetricsProbe
) {

    private val log: Logger = LoggerFactory.getLogger(Producer::class.java)
    private val objectMapper = jacksonMapperBuilder()
        .addModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build()

    fun varselAktivert(beskjed: Beskjed) {
        objectMapper.valueToTree<ObjectNode>(beskjed)
            .let { varselAktivert(it, BESKJED, beskjed.eventId) }
    }

    fun varselAktivert(oppgave: Oppgave) {
        objectMapper.valueToTree<ObjectNode>(oppgave)
            .let { varselAktivert(it, OPPGAVE, oppgave.eventId) }
    }

    fun varselAktivert(innboks: Innboks) {
        objectMapper.valueToTree<ObjectNode>(innboks)
            .let { varselAktivert(it, INNBOKS, innboks.eventId) }
    }

    private fun varselAktivert(varsel: ObjectNode, varselType: VarselType, eventId: String) {
        varsel.put("@event_name", Aktivert.lowerCaseName)
        varsel.put("varselType", varselType.eventType)
        varsel.put("tidspunkt", nowAtUtc().toString())
        val producerRecord = ProducerRecord(topicName, eventId, varsel.toString())
        kafkaProducer.send(producerRecord)
        runBlocking {
            rapidMetricsProbe.countVarselAktivertProduced()
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
