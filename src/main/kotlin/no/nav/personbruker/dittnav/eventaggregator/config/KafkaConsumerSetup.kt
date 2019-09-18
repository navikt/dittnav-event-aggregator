package no.nav.personbruker.dittnav.eventaggregator.config

import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Informasjon
import no.nav.brukernotifikasjon.schemas.Melding
import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.kafka.Consumer
import no.nav.personbruker.dittnav.eventaggregator.service.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.service.impl.EventToConsoleBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.service.impl.InformasjonEventService
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

object KafkaConsumerSetup {

    private val log : Logger = LoggerFactory.getLogger(KafkaConsumerSetup::class.java)

    fun initializeTheKafkaConsumers(environment: Environment) {
        initKafkaConsumers(environment)
        startAllKafkaPollers()
    }

    fun initKafkaConsumers(environment: Environment) {
        Server.infoConsumer = setupConsumerForTheInformasjonTopic(environment)
        Server.oppgaveConsumer = setupConsumerForTheOppgaveTopic(environment)
        Server.meldingConsumer = setupConsumerForTheMeldingTopic(environment)
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

    fun setupConsumerForTheInformasjonTopic(environment: Environment): Consumer<Informasjon> {
        val eventProcessor = InformasjonEventService(Server.database)
        val kafkaProps = Kafka.consumerProps(environment, "informasjon")
        return setupConsumerForTheInformasjonTopic(kafkaProps, eventProcessor)
    }

    fun setupConsumerForTheInformasjonTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<Informasjon>): Consumer<Informasjon> {
        val kafkaConsumer = KafkaConsumer<String, Informasjon>(kafkaProps)
        return Consumer(Kafka.informasjonTopicName, kafkaConsumer, eventProcessor)
    }

    fun setupConsumerForTheOppgaveTopic(environment: Environment): Consumer<Oppgave> {
        val eventProcessor = EventToConsoleBatchProcessorService<Oppgave>()
        val kafkaProps = Kafka.consumerProps(environment, "oppgave")
        return setupConsumerForTheOppgaveTopic(kafkaProps, eventProcessor)
    }

    fun setupConsumerForTheOppgaveTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<Oppgave>): Consumer<Oppgave> {
        val kafkaConsumer = KafkaConsumer<String, Oppgave>(kafkaProps)
        return Consumer(Kafka.oppgaveTopicName, kafkaConsumer, eventProcessor)
    }

    fun setupConsumerForTheMeldingTopic(environment: Environment): Consumer<Melding> {
        val eventProcessor = EventToConsoleBatchProcessorService<Melding>()
        val kafkaProps = Kafka.consumerProps(environment, "melding")
        return setupConsumerForTheMeldingTopic(kafkaProps, eventProcessor)
    }

    fun setupConsumerForTheMeldingTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<Melding>): Consumer<Melding> {
        val kafkaConsumer = KafkaConsumer<String, Melding>(kafkaProps)
        return Consumer(Kafka.meldingTopicName, kafkaConsumer, eventProcessor)
    }

}
