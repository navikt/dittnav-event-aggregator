package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.brukernotifikasjon.schemas.Done
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.Instant

object DoneProducer {

    fun produceEvent(messagesCount: Int = 1) {
        KafkaProducer<String, Done>(Kafka.producerProps(Environment(), EventType.DONE)).use { producer ->
            for (i in 0 until messagesCount) {
                producer.send(ProducerRecord(Kafka.doneTopicName, createDone(i)))
            }
        }
    }

    private fun createDone(i: Int): Done {
        val build = Done.newBuilder()
                .setAktorId("12345")
                .setDokumentId("300$i")
                .setEventId("$i")
                .setProdusent("DittNAV")
                .setTidspunkt(Instant.now().toEpochMilli())
        return build.build()
    }

}
