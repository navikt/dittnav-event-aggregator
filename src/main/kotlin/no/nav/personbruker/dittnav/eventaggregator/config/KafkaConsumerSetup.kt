package no.nav.personbruker.dittnav.eventaggregator.config

import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Beskjed
import no.nav.brukernotifikasjon.schemas.Innboks
import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.Consumer
import no.nav.personbruker.dittnav.eventaggregator.done.DoneEventService
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedEventService
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksEventService
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksRepository
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveEventService
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

object KafkaConsumerSetup {

    private val log: Logger = LoggerFactory.getLogger(KafkaConsumerSetup::class.java)

    fun startAllKafkaPollers(appContext: ApplicationContext) {
        appContext.beskjedConsumer.startPolling()
        appContext.oppgaveConsumer.startPolling()
        appContext.innboksConsumer.startPolling()
        appContext.doneConsumer.startPolling()
    }

    fun stopAllKafkaConsumers(appContext: ApplicationContext) {
        log.info("Begynner å stoppe kafka-pollerne...")
        appContext.beskjedConsumer.stopPolling()
        appContext.oppgaveConsumer.stopPolling()
        appContext.innboksConsumer.stopPolling()
        appContext.doneConsumer.stopPolling()
        log.info("...ferdig med å stoppe kafka-pollerne.")
    }


    fun setupConsumerForTheBeskjedTopic(environment: Environment, database: Database): Consumer<Beskjed> {
        val beskjedRepository = BeskjedRepository(database)
        val eventProcessor = BeskjedEventService(beskjedRepository)
        val kafkaProps = Kafka.consumerProps(environment, EventType.BESKJED)
        return setupConsumerForTheBeskjedTopic(kafkaProps, eventProcessor)
    }

    fun setupConsumerForTheBeskjedTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<Beskjed>): Consumer<Beskjed> {
        val kafkaConsumer = KafkaConsumer<String, Beskjed>(kafkaProps)
        return Consumer(Kafka.beskjedTopicName, kafkaConsumer, eventProcessor)
    }

    fun setupConsumerForTheOppgaveTopic(environment: Environment, database: Database): Consumer<Oppgave> {
        val eventProcessor = OppgaveEventService(database)
        val kafkaProps = Kafka.consumerProps(environment, EventType.OPPGAVE)
        return setupConsumerForTheOppgaveTopic(kafkaProps, eventProcessor)
    }

    fun setupConsumerForTheOppgaveTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<Oppgave>): Consumer<Oppgave> {
        val kafkaConsumer = KafkaConsumer<String, Oppgave>(kafkaProps)
        return Consumer(Kafka.oppgaveTopicName, kafkaConsumer, eventProcessor)
    }

    fun setupConsumerForTheInnboksTopic(environment: Environment, database: Database): Consumer<Innboks> {
        val innboksRepository = InnboksRepository(database)
        val eventProcessor = InnboksEventService(innboksRepository)
        val kafkaProps = Kafka.consumerProps(environment, EventType.INNBOKS)
        return setupConsumerForTheInnboksTopic(kafkaProps, eventProcessor)
    }

    fun setupConsumerForTheInnboksTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<Innboks>): Consumer<Innboks> {
        val kafkaConsumer = KafkaConsumer<String, Innboks>(kafkaProps)
        return Consumer(Kafka.innboksTopicName, kafkaConsumer, eventProcessor)
    }

    fun setupConsumerForTheDoneTopic(environment: Environment, database: Database): Consumer<Done> {
        val eventProcessor = DoneEventService(database)
        val kafkaProps = Kafka.consumerProps(environment, EventType.DONE)
        return setupConsumerForTheDoneTopic(kafkaProps, eventProcessor)
    }

    fun setupConsumerForTheDoneTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<Done>): Consumer<Done> {
        val kafkaConsumer = KafkaConsumer<String, Done>(kafkaProps)
        return Consumer(Kafka.doneTopicName, kafkaConsumer, eventProcessor)
    }
}
