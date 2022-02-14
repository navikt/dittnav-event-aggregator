package no.nav.personbruker.dittnav.eventaggregator.config

import no.nav.brukernotifikasjon.schemas.input.DoneInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedEventService
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonPersistingService
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.KafkaProducerWrapper
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.polling.PeriodicConsumerPollingCheck
import no.nav.personbruker.dittnav.eventaggregator.done.DoneEventService
import no.nav.personbruker.dittnav.eventaggregator.done.DonePersistingService
import no.nav.personbruker.dittnav.eventaggregator.done.DoneRepository
import no.nav.personbruker.dittnav.eventaggregator.done.PeriodicDoneEventWaitingTableProcessor
import no.nav.personbruker.dittnav.eventaggregator.expired.DoneEventEmitter
import no.nav.personbruker.dittnav.eventaggregator.expired.PeriodicExpiredNotificationProcessor
import no.nav.personbruker.dittnav.eventaggregator.expired.ExpiredPersistingService
import no.nav.personbruker.dittnav.eventaggregator.health.HealthService
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksEventService
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksRepository
import no.nav.personbruker.dittnav.eventaggregator.metrics.buildDBMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.buildEventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveEventService
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveRepository
import no.nav.personbruker.dittnav.eventaggregator.statusoppdatering.StatusoppdateringEventService
import no.nav.personbruker.dittnav.eventaggregator.statusoppdatering.StatusoppdateringRepository
import org.apache.kafka.clients.producer.KafkaProducer
import org.slf4j.LoggerFactory

class ApplicationContext {

    private val log = LoggerFactory.getLogger(ApplicationContext::class.java)

    val environment = Environment()
    val database: Database = PostgresDatabase(environment)

    val eventMetricsProbe = buildEventMetricsProbe(environment)
    val dbMetricsProbe = buildDBMetricsProbe(environment)

    val beskjedRepository = BeskjedRepository(database)
    val beskjedPersistingService = BrukernotifikasjonPersistingService(beskjedRepository)
    val beskjedEventProcessor = BeskjedEventService(beskjedPersistingService, eventMetricsProbe)
    val beskjedKafkaProps = Kafka.consumerProps(environment, EventType.BESKJED_INTERN)
    var beskjedConsumer = initializeBeskjedConsumer()

    val oppgaveRepository = OppgaveRepository(database)
    val oppgavePersistingService = BrukernotifikasjonPersistingService(oppgaveRepository)
    val oppgaveEventProcessor = OppgaveEventService(oppgavePersistingService, eventMetricsProbe)
    val oppgaveKafkaProps = Kafka.consumerProps(environment, EventType.OPPGAVE_INTERN)
    var oppgaveConsumer = initializeOppgaveConsumer()

    val innboksRepository = InnboksRepository(database)
    val innboksPersistingService = BrukernotifikasjonPersistingService(innboksRepository)
    val innboksEventProcessor = InnboksEventService(innboksPersistingService, eventMetricsProbe)
    val innboksKafkaProps = Kafka.consumerProps(environment, EventType.INNBOKS_INTERN)
    var innboksConsumer = initializeInnboksConsumer()

    val doneRepository = DoneRepository(database)
    val donePersistingService = DonePersistingService(doneRepository)
    val doneEventService = DoneEventService(donePersistingService, eventMetricsProbe)
    val doneKafkaProps = Kafka.consumerProps(environment, EventType.DONE_INTERN)
    var doneConsumer = initializeDoneConsumer()

    val statusoppdateringRepository = StatusoppdateringRepository(database)
    val statusoppdateringPersistingService = BrukernotifikasjonPersistingService(statusoppdateringRepository)
    val statusoppdateringEventProcessor = StatusoppdateringEventService(statusoppdateringPersistingService, eventMetricsProbe)
    val statusoppdateringKafkaProps = Kafka.consumerProps(environment, EventType.STATUSOPPDATERING_INTERN)
    var statusoppdateringConsumer = initializeStatusoppdateringConsumer()

    var periodicDoneEventWaitingTableProcessor = initializeDoneWaitingTableProcessor()
    var periodicConsumerPollingCheck = initializePeriodicConsumerPollingCheck()

    val expiredPersistingService = ExpiredPersistingService(database)
    val kafkaProducerDone = KafkaProducerWrapper(environment.doneInputTopicName, KafkaProducer<NokkelInput, DoneInput>(Kafka.producerProps(environment)))
    val doneEventEmitter = DoneEventEmitter(kafkaProducerDone)
    var periodicExpiredBeskjedProcessor = initializeExpiredBeskjedProcessor()

    val healthService = HealthService(this)

    private fun initializeBeskjedConsumer() =
            KafkaConsumerSetup.setupConsumerForTheBeskjedTopic(beskjedKafkaProps, beskjedEventProcessor, environment.beskjedInternTopicName)

    private fun initializeOppgaveConsumer() =
            KafkaConsumerSetup.setupConsumerForTheOppgaveTopic(oppgaveKafkaProps, oppgaveEventProcessor, environment.oppgaveInternTopicName)

    private fun initializeInnboksConsumer() =
            KafkaConsumerSetup.setupConsumerForTheInnboksTopic(innboksKafkaProps, innboksEventProcessor, environment.innboksInternTopicName)

    private fun initializeDoneConsumer() =
            KafkaConsumerSetup.setupConsumerForTheDoneTopic(doneKafkaProps, doneEventService, environment.doneInternTopicName)

    private fun initializeStatusoppdateringConsumer() =
            KafkaConsumerSetup.setupConsumerForTheStatusoppdateringTopic(statusoppdateringKafkaProps, statusoppdateringEventProcessor, environment.statusoppdateringInternTopicName)

    private fun initializeDoneWaitingTableProcessor() = PeriodicDoneEventWaitingTableProcessor(donePersistingService, dbMetricsProbe)

    private fun initializePeriodicConsumerPollingCheck() = PeriodicConsumerPollingCheck(this)

    private fun initializeExpiredBeskjedProcessor() =
        PeriodicExpiredNotificationProcessor(expiredPersistingService, doneEventEmitter)

    fun reinitializeConsumers() {
        if (beskjedConsumer.isCompleted()) {
            beskjedConsumer = initializeBeskjedConsumer()
            log.info("beskjedConsumer har blitt reinstansiert.")
        } else {
            log.warn("beskjedConsumer kunne ikke bli reinstansiert fordi den fortsatt er aktiv.")
        }

        if (oppgaveConsumer.isCompleted()) {
            oppgaveConsumer = initializeOppgaveConsumer()
            log.info("oppgaveConsumer har blitt reinstansiert.")
        } else {
            log.warn("oppgaveConsumer kunne ikke bli reinstansiert fordi den fortsatt er aktiv.")
        }

        if (innboksConsumer.isCompleted()) {
            innboksConsumer = initializeInnboksConsumer()
            log.info("innboksConsumer har blitt reinstansiert.")
        } else {
            log.warn("innboksConsumer kunne ikke bli reinstansiert fordi den fortsatt er aktiv.")
        }

        if (doneConsumer.isCompleted()) {
            doneConsumer = initializeDoneConsumer()
            log.info("doneConsumer har blitt reinstansiert.")
        } else {
            log.warn("doneConsumer kunne ikke bli reinstansiert fordi den fortsatt er aktiv.")
        }

        if (statusoppdateringConsumer.isCompleted()) {
            statusoppdateringConsumer = initializeStatusoppdateringConsumer()
            log.info("statusoppdateringConsumer har blitt reinstansiert.")
        } else {
            log.warn("statusoppdateringConsumer kunne ikke bli reinstansiert fordi den fortsatt er aktiv.")
        }
    }

    fun reinitializeDoneWaitingTableProcessor() {
        if (periodicDoneEventWaitingTableProcessor.isCompleted()) {
            periodicDoneEventWaitingTableProcessor = initializeDoneWaitingTableProcessor()
            log.info("periodicDoneEventWaitingTableProcessor har blitt reinstansiert.")
        } else {
            log.warn("periodicDoneEventWaitingTableProcessor kunne ikke bli reinstansiert fordi den fortsatt er aktiv.")
        }
    }

    fun reinitializePeriodicConsumerPollingCheck() {
        if (periodicConsumerPollingCheck.isCompleted()) {
            periodicConsumerPollingCheck = initializePeriodicConsumerPollingCheck()
            log.info("periodicConsumerPollingCheck har blitt reinstansiert.")
        } else {
            log.warn("periodicConsumerPollingCheck kunne ikke bli reinstansiert fordi den fortsatt er aktiv.")
        }
    }

}
