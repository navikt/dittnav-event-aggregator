package no.nav.personbruker.dittnav.eventaggregator.config

import no.nav.brukernotifikasjon.schemas.internal.*
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
        if (isOtherEnvironmentThanProd()) {
            appContext.innboksConsumer.startPolling()
            appContext.statusoppdateringConsumer.startPolling()
        } else {
            log.info("Er i produksjonsmiljø, unnlater å starte innboksconsumer.")
        }
    }

    suspend fun stopAllKafkaConsumers(appContext: ApplicationContext) {
        log.info("Begynner å stoppe kafka-pollerne...")
        appContext.beskjedConsumer.stopPolling()
        appContext.oppgaveConsumer.stopPolling()
        appContext.doneConsumer.stopPolling()
        if (isOtherEnvironmentThanProd()) {
            appContext.innboksConsumer.stopPolling()
            appContext.statusoppdateringConsumer.stopPolling()
        }
        log.info("...ferdig med å stoppe kafka-pollerne.")
    }

    suspend fun restartPolling(appContext: ApplicationContext) {
        stopAllKafkaConsumers(appContext)
        appContext.reinitializeConsumers()
        startAllKafkaPollers(appContext)
    }

    fun setupConsumerForTheBeskjedTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<BeskjedIntern>): Consumer<BeskjedIntern> {
        val kafkaConsumer = KafkaConsumer<NokkelIntern, BeskjedIntern>(kafkaProps)
        return Consumer(Kafka.beskjedHovedTopicName, kafkaConsumer, eventProcessor)
    }

    fun setupConsumerForTheOppgaveTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<OppgaveIntern>): Consumer<OppgaveIntern> {
        val kafkaConsumer = KafkaConsumer<NokkelIntern, OppgaveIntern>(kafkaProps)
        return Consumer(Kafka.oppgaveHovedTopicName, kafkaConsumer, eventProcessor)
    }

    fun setupConsumerForTheInnboksTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<InnboksIntern>): Consumer<InnboksIntern> {
        val kafkaConsumer = KafkaConsumer<NokkelIntern, InnboksIntern>(kafkaProps)
        return Consumer(Kafka.innboksHovedTopicName, kafkaConsumer, eventProcessor)
    }

    fun setupConsumerForTheDoneTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<DoneIntern>): Consumer<DoneIntern> {
        val kafkaConsumer = KafkaConsumer<NokkelIntern, DoneIntern>(kafkaProps)
        return Consumer(Kafka.doneHovedTopicName, kafkaConsumer, eventProcessor)
    }

    fun setupConsumerForTheStatusoppdateringTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<StatusoppdateringIntern>): Consumer<StatusoppdateringIntern> {
        val kafkaConsumer = KafkaConsumer<NokkelIntern, StatusoppdateringIntern>(kafkaProps)
        return Consumer(Kafka.statusoppdateringHovedTopicName, kafkaConsumer, eventProcessor)
    }

    fun <T> createCountConsumer(eventType: EventType,
                                topic: String,
                                environment: Environment,
                                enableSecurity: Boolean = ConfigUtil.isCurrentlyRunningOnNais()): KafkaConsumer<NokkelIntern, T> {

        val kafkaProps = Kafka.counterConsumerProps(environment, eventType, enableSecurity)
        val consumer = KafkaConsumer<NokkelIntern, T>(kafkaProps)
        consumer.subscribe(listOf(topic))
        return consumer
    }

}
