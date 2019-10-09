package no.nav.personbruker.dittnav.eventaggregator.kafka

import no.nav.brukernotifikasjon.schemas.*
import no.nav.personbruker.dittnav.eventaggregator.config.*

import no.nav.personbruker.dittnav.eventaggregator.config.Kafka.doneTopicName
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka.informasjonTopicName
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka.meldingTopicName
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka.oppgaveTopicName

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.Instant

object Producer {

    fun produceInformasjonEvent(messagesCount: Int = 1) {
        KafkaProducer<String, Informasjon>(Kafka.producerProps(Environment(), EventType.INFORMASJON)).use { producer ->
            for (i in 0 until messagesCount) {
                producer.send(ProducerRecord(informasjonTopicName, createInformasjon(i)))
            }
        }
    }

    fun produceOppgaveEvent(messagesCount: Int = 1) {
        KafkaProducer<String, Oppgave>(Kafka.producerProps(Environment(), EventType.OPPGAVE)).use { producer ->
            for (i in 0 until messagesCount) {
                producer.send(ProducerRecord(oppgaveTopicName, createOppgave(i)))
            }
        }
    }

    fun produceMeldingEvent(messagesCount: Int = 1) {
        KafkaProducer<String, Melding>(Kafka.producerProps(Environment(), EventType.MELDING)).use { producer ->
            for (i in 0 until messagesCount) {
                producer.send(ProducerRecord(meldingTopicName, createMelding(i)))
            }
        }
    }

    fun produceDoneEvent(messagesCount: Int = 1) {
        KafkaProducer<String, Done>(Kafka.producerProps(Environment(), EventType.DONE)).use { producer ->
            for (i in 0 until messagesCount) {
                producer.send(ProducerRecord(doneTopicName, createDone(i)))
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
