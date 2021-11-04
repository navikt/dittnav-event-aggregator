package no.nav.personbruker.dittnav.eventaggregator.config

import no.nav.brukernotifikasjon.schemas.*
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

object KafkaConsumerSetup {

    private val log: Logger = LoggerFactory.getLogger(KafkaConsumerSetup::class.java)

    fun startAllKafkaPollers(appContext: ApplicationContext) {
        appContext.beskjedConsumer.startPolling()
        appContext.oppgaveConsumer.startPolling()
        appContext.doneConsumer.startPolling()
        appContext.innboksConsumer.startPolling()
        if (isOtherEnvironmentThanProd()) {
            appContext.statusoppdateringConsumer.startPolling()
        } else {
            log.info("Er i produksjonsmiljø, unnlater å starte statusoppdatering-consumer.")
        }
    }

    suspend fun stopAllKafkaConsumers(appContext: ApplicationContext) {
        log.info("Begynner å stoppe kafka-pollerne...")
        appContext.beskjedConsumer.stopPolling()
        appContext.oppgaveConsumer.stopPolling()
        appContext.doneConsumer.stopPolling()
        appContext.innboksConsumer.stopPolling()
        if (isOtherEnvironmentThanProd()) {
            appContext.statusoppdateringConsumer.stopPolling()
        }
        log.info("...ferdig med å stoppe kafka-pollerne.")
    }

    suspend fun restartPolling(appContext: ApplicationContext) {
        stopAllKafkaConsumers(appContext)
        appContext.reinitializeConsumers()
        startAllKafkaPollers(appContext)
    }

    fun setupConsumerForTheBeskjedTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<Beskjed>): Consumer<Beskjed> {
        val kafkaConsumer = KafkaConsumer<Nokkel, Beskjed>(kafkaProps)
        return Consumer(Kafka.beskjedTopicName, kafkaConsumer, eventProcessor)
    }

    fun setupConsumerForTheOppgaveTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<Oppgave>): Consumer<Oppgave> {
        val kafkaConsumer = KafkaConsumer<Nokkel, Oppgave>(kafkaProps)
        return Consumer(Kafka.oppgaveTopicName, kafkaConsumer, eventProcessor)
    }

    fun setupConsumerForTheInnboksTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<Innboks>): Consumer<Innboks> {
        val kafkaConsumer = KafkaConsumer<Nokkel, Innboks>(kafkaProps)
        return Consumer(Kafka.innboksTopicName, kafkaConsumer, eventProcessor)
    }

    fun setupConsumerForTheDoneTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<Done>): Consumer<Done> {
        val kafkaConsumer = KafkaConsumer<Nokkel, Done>(kafkaProps)
        return Consumer(Kafka.doneTopicName, kafkaConsumer, eventProcessor)
    }

    fun setupConsumerForTheStatusoppdateringTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<Statusoppdatering>): Consumer<Statusoppdatering> {
        val kafkaConsumer = KafkaConsumer<Nokkel, Statusoppdatering>(kafkaProps)
        return Consumer(Kafka.statusoppdateringTopicName, kafkaConsumer, eventProcessor)
    }

    fun <T> createCountConsumer(eventType: EventType,
                                topic: String,
                                environment: Environment,
                                enableSecurity: Boolean = ConfigUtil.isCurrentlyRunningOnNais()): KafkaConsumer<Nokkel, T> {

        val kafkaProps = Kafka.counterConsumerProps(environment, eventType, enableSecurity)
        val consumer = KafkaConsumer<Nokkel, T>(kafkaProps)
        consumer.subscribe(listOf(topic))
        return consumer
    }

}
