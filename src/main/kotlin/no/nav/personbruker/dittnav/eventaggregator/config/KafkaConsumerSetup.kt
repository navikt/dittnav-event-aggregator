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
        if (shouldPollBeskjed()) {
            appContext.beskjedConsumer.startPolling()
        } else {
            log.info("Unnlater å starte polling av beskjed")
        }

        if (shouldPollOppgave()) {
            appContext.oppgaveConsumer.startPolling()
        } else {
            log.info("Unnlater å starte polling av oppgave")
        }

        if (shouldPollDone()) {
            appContext.doneConsumer.startPolling()
        } else {
            log.info("Unnlater å starte polling av done")
        }

        if (isOtherEnvironmentThanProd()) {
            if (shouldPollInnboks()) {
                appContext.innboksConsumer.startPolling()
            } else {
                log.info("Unnlater å starte polling av innboks")
            }

            if (shouldPollStatusoppdatering()) {
                appContext.statusoppdateringConsumer.startPolling()
            } else {
                log.info("Unnlater å starte polling av statusoppdatering")
            }
        } else {
            log.info("Er i produksjonsmiljø, unnlater å starte innboksconsumer og statusoppdateringsconsumer.")
        }
    }

    suspend fun stopAllKafkaConsumers(appContext: ApplicationContext) {
        log.info("Begynner å stoppe kafka-pollerne...")

        if (!appContext.beskjedConsumer.isCompleted()) {
            appContext.beskjedConsumer.stopPolling()
        }

        if (!appContext.oppgaveConsumer.isCompleted()) {
            appContext.oppgaveConsumer.stopPolling()
        }

        if (!appContext.oppgaveConsumer.isCompleted()) {
            appContext.oppgaveConsumer.stopPolling()
        }

        if (!appContext.doneConsumer.isCompleted()) {
            appContext.doneConsumer.stopPolling()
        }

        if (isOtherEnvironmentThanProd()) {
            if (!appContext.innboksConsumer.isCompleted()) {
                appContext.innboksConsumer.stopPolling()
            }

            if (!appContext.statusoppdateringConsumer.isCompleted()) {
                appContext.statusoppdateringConsumer.stopPolling()
            }
        }
        log.info("...ferdig med å stoppe kafka-pollerne.")
    }

    suspend fun restartPolling(appContext: ApplicationContext) {
        stopAllKafkaConsumers(appContext)
        appContext.reinitializeConsumers()
        startAllKafkaPollers(appContext)
    }

    fun setupConsumerForTheBeskjedTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<BeskjedIntern>, topic: String): Consumer<BeskjedIntern> {
        val kafkaConsumer = KafkaConsumer<NokkelIntern, BeskjedIntern>(kafkaProps)
        return Consumer(topic, kafkaConsumer, eventProcessor)
    }

    fun setupConsumerForTheOppgaveTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<OppgaveIntern>, topic: String): Consumer<OppgaveIntern> {
        val kafkaConsumer = KafkaConsumer<NokkelIntern, OppgaveIntern>(kafkaProps)
        return Consumer(topic, kafkaConsumer, eventProcessor)
    }

    fun setupConsumerForTheInnboksTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<InnboksIntern>, topic: String): Consumer<InnboksIntern> {
        val kafkaConsumer = KafkaConsumer<NokkelIntern, InnboksIntern>(kafkaProps)
        return Consumer(topic, kafkaConsumer, eventProcessor)
    }

    fun setupConsumerForTheDoneTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<DoneIntern>, topic: String): Consumer<DoneIntern> {
        val kafkaConsumer = KafkaConsumer<NokkelIntern, DoneIntern>(kafkaProps)
        return Consumer(topic, kafkaConsumer, eventProcessor)
    }

    fun setupConsumerForTheStatusoppdateringTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<StatusoppdateringIntern>, topic: String): Consumer<StatusoppdateringIntern> {
        val kafkaConsumer = KafkaConsumer<NokkelIntern, StatusoppdateringIntern>(kafkaProps)
        return Consumer(topic, kafkaConsumer, eventProcessor)
    }
}
