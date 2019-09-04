package no.nav.personbruker.dittnav.eventaggregator.config

import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Informasjon
import no.nav.brukernotifikasjon.schemas.Melding
import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.kafka.Consumer
import no.nav.personbruker.dittnav.eventaggregator.service.impl.EventToConsoleBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.service.impl.InformasjonEventService
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object KafkaConsumerSetup {

    private val log : Logger = LoggerFactory.getLogger(KafkaConsumerSetup::class.java)

    fun initializeTheKafkaConsumers() {
        initKafkaConsumers()
        startAllKafkaPollers()
    }

    fun initKafkaConsumers() {
        Server.infoConsumer = setupConsumerForTheInformasjonTopic()
        Server.oppgaveConsumer = setupConsumerForTheOppgaveTopic()
        Server.meldingConsumer = setupConsumerForTheMeldingTopic()
    }

    fun startAllKafkaPollers() {
        Server.infoConsumer.poll()
        Server.oppgaveConsumer.poll()
        Server.meldingConsumer.poll()
    }

    fun stopAllKafkaConsumers() = runBlocking {
        log.info("Begynner å stoppe kafka-pollerne...")
        Server.infoConsumer.cancel()
        Server.oppgaveConsumer.cancel()
        Server.meldingConsumer.cancel()
        log.info("...ferdig med å stoppe kafka-pollerne.")
    }

    fun setupConsumerForTheInformasjonTopic(): Consumer<Informasjon> {
        val eventProcessor = InformasjonEventService(Server.database)
        val kafkaProps = Kafka.consumerProps(Server.environment, "informasjon")
        val kafkaConsumer = KafkaConsumer<String, Informasjon>(kafkaProps)
        return Consumer(Kafka.informasjonTopicName, kafkaConsumer, eventProcessor)
    }

    fun setupConsumerForTheOppgaveTopic(): Consumer<Oppgave> {
        val eventProcessor = EventToConsoleBatchProcessorService<Oppgave>()
        val kafkaProps = Kafka.consumerProps(Server.environment, "oppgave")
        val kafkaConsumer = KafkaConsumer<String, Oppgave>(kafkaProps)
        return Consumer(Kafka.oppgaveTopicName, kafkaConsumer, eventProcessor)
    }

    fun setupConsumerForTheMeldingTopic(): Consumer<Melding> {
        val eventProcessor = EventToConsoleBatchProcessorService<Melding>()
        val kafkaProps = Kafka.consumerProps(Server.environment, "melding")
        val kafkaConsumer = KafkaConsumer<String, Melding>(kafkaProps)
        return Consumer(Kafka.meldingTopicName, kafkaConsumer, eventProcessor)
    }

}
