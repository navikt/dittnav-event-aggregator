package no.nav.personbruker.dittnav.eventaggregator.melding

import no.nav.brukernotifikasjon.schemas.Melding
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.Instant

object MeldingProducer {

    fun produceEvent(messagesCount: Int = 1) {
        KafkaProducer<String, Melding>(Kafka.producerProps(Environment(), EventType.MELDING)).use { producer ->
            for (i in 0 until messagesCount) {
                producer.send(ProducerRecord(Kafka.meldingTopicName, createMelding(i)))
            }
        }
    }

    private fun createMelding(i: Int): Melding {
        val build = Melding.newBuilder()
                .setAktorId("12345")
                .setDokumentId("300$i")
                .setEventId("$i")
                .setProdusent("DittNAV")
                .setLink("https://nav.no/systemX/$i")
                .setTekst("Du har fått en ny melding")
                .setTidspunkt(Instant.now().toEpochMilli())
                .setSikkerhetsniva(4)
        return build.build()
    }

}
