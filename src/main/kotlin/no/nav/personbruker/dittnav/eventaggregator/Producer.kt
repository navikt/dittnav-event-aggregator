package no.nav.personbruker.dittnav.eventaggregator

import no.nav.personbruker.dittnav.skjema.Informasjon
import no.nav.personbruker.dittnav.skjema.Melding
import no.nav.personbruker.dittnav.skjema.Oppgave
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.Instant

object Producer {

    val topicBaseName = "example.topic.dittnav"
    val oppgaveTopicName = "$topicBaseName.oppgave"
    val meldingTopicName = "$topicBaseName.melding"
    val informasjonTopicName = "$topicBaseName.informasjon"

    fun produceInformasjonEvent(messagesCount: Int = 1) {
        KafkaProducer<String, Informasjon>(Config.producerProps(Environment())).use { producer ->
            for (i in 0 until messagesCount) {
                producer.send(ProducerRecord(informasjonTopicName, createInformasjon(i)))
            }
        }
    }

    fun produceOppgaveEvent(messagesCount: Int = 1) {
        KafkaProducer<String, Oppgave>(Config.producerProps(Environment())).use { producer ->
            for (i in 0 until messagesCount) {
                producer.send(ProducerRecord(oppgaveTopicName, createOppgave(i)))
            }
        }
    }

    fun produceMeldingEvent(messagesCount: Int = 1) {
        KafkaProducer<String, Melding>(Config.producerProps(Environment())).use { producer ->
            for (i in 0 until messagesCount) {
                producer.send(ProducerRecord(meldingTopicName, createMelding(i)))
            }
        }
    }

    private fun createInformasjon(i: Int): Informasjon {
        val build = Informasjon.newBuilder()
                .setAktorId("12345")
                .setDokumentId("100" + i)
                .setEventId("" + i)
                .setProdusent("DittNAV")
                .setLink("https://nav.no/systemX/" + i)
                .setTekst("Dette er informasjon til brukeren")
                .setTidspunkt(Instant.now().toEpochMilli())
                .setSikkerhetsniva(4)
        return build.build()
    }

    private fun createOppgave(i: Int): Oppgave {
        val build = Oppgave.newBuilder()
                .setAktorId("12345")
                .setDokumentId("200" + i)
                .setEventId("" + i)
                .setProdusent("DittNAV")
                .setLink("https://nav.no/systemX/" + i)
                .setTekst("Dette er noe en bruker må gjøre")
                .setTidspunkt(Instant.now().toEpochMilli())
                .setSikkerhetsniva(4)
        return build.build()
    }

    private fun createMelding(i: Int): Melding {
        val build = Melding.newBuilder()
                .setAktorId("12345")
                .setDokumentId("300" + i)
                .setEventId("" + i)
                .setProdusent("DittNAV")
                .setLink("https://nav.no/systemX/" + i)
                .setTekst("Du har fått en ny melding")
                .setTidspunkt(Instant.now().toEpochMilli())
                .setSikkerhetsniva(4)
        return build.build()
    }

}
