package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.Instant

object OppgaveProducer {

    fun produceEvent(messagesCount: Int = 1) {
        KafkaProducer<String, Oppgave>(Kafka.producerProps(Environment(), EventType.OPPGAVE)).use { producer ->
            for (i in 0 until messagesCount) {
                producer.send(ProducerRecord(Kafka.oppgaveTopicName, createOppgave(i)))
            }
        }
    }

    private fun createOppgave(i: Int): Oppgave {
        val build = Oppgave.newBuilder()
                .setAktorId("12345")
                .setDokumentId("200$i")
                .setEventId("$i")
                .setProdusent("DittNAV")
                .setLink("https://nav.no/systemX/$i")
                .setTekst("Dette er noe en bruker må gjøre")
                .setTidspunkt(Instant.now().toEpochMilli())
                .setSikkerhetsniva(4)
        return build.build()
    }

}
