package no.nav.personbruker.dittnav.eventaggregator.informasjon

import no.nav.brukernotifikasjon.schemas.Informasjon
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.Instant

object InformasjonProducer {

    fun produceEvent(messagesCount: Int = 1) {
        KafkaProducer<String, Informasjon>(Kafka.producerProps(Environment(), EventType.INFORMASJON)).use { producer ->
            for (i in 0 until messagesCount) {
                producer.send(ProducerRecord(Kafka.informasjonTopicName, createInformasjon(i)))
            }
        }
    }

    private fun createInformasjon(i: Int): Informasjon {
        val build = Informasjon.newBuilder()
                .setAktorId("12345")
                .setDokumentId("100$i")
                .setEventId("$i")
                .setProdusent("DittNAV")
                .setLink("https://nav.no/systemX/$i")
                .setTekst("Dette er informasjon til brukeren")
                .setTidspunkt(Instant.now().toEpochMilli())
                .setSikkerhetsniva(4)
        return build.build()
    }

}
