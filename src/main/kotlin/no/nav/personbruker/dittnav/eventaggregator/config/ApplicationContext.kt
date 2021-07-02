package no.nav.personbruker.dittnav.eventaggregator.config

import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedEventService
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonPersistingService
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.polling.PeriodicConsumerPollingCheck
import no.nav.personbruker.dittnav.eventaggregator.done.DoneEventService
import no.nav.personbruker.dittnav.eventaggregator.done.DonePersistingService
import no.nav.personbruker.dittnav.eventaggregator.done.DoneRepository
import no.nav.personbruker.dittnav.eventaggregator.done.PeriodicDoneEventWaitingTableProcessor
import no.nav.personbruker.dittnav.eventaggregator.health.HealthService
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksEventService
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksRepository
import no.nav.personbruker.dittnav.eventaggregator.metrics.ProducerNameResolver
import no.nav.personbruker.dittnav.eventaggregator.metrics.ProducerNameScrubber
import no.nav.personbruker.dittnav.eventaggregator.metrics.buildDBMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.buildEventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveEventService
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveRepository
import no.nav.personbruker.dittnav.eventaggregator.statusoppdatering.StatusoppdateringEventService
import no.nav.personbruker.dittnav.eventaggregator.statusoppdatering.StatusoppdateringRepository
import org.slf4j.LoggerFactory

class ApplicationContext {

    private val log = LoggerFactory.getLogger(ApplicationContext::class.java)

    val environment = Environment()
    val database: Database = PostgresDatabase(environment)

    val nameResolver = ProducerNameResolver(database)
    val nameScrubber = ProducerNameScrubber(nameResolver)
    val eventMetricsProbe = buildEventMetricsProbe(environment, nameScrubber)
    val dbMetricsProbe = buildDBMetricsProbe(environment, nameScrubber)

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

    val healthService = HealthService(this)

    private fun initializeBeskjedConsumer() =
            KafkaConsumerSetup.setupConsumerForTheBeskjedTopic(beskjedKafkaProps, beskjedEventProcessor)

    private fun initializeOppgaveConsumer() =
            KafkaConsumerSetup.setupConsumerForTheOppgaveTopic(oppgaveKafkaProps, oppgaveEventProcessor)

    private fun initializeInnboksConsumer() =
            KafkaConsumerSetup.setupConsumerForTheInnboksTopic(innboksKafkaProps, innboksEventProcessor)

    private fun initializeDoneConsumer() =
            KafkaConsumerSetup.setupConsumerForTheDoneTopic(doneKafkaProps, doneEventService)

    private fun initializeStatusoppdateringConsumer() =
            KafkaConsumerSetup.setupConsumerForTheStatusoppdateringTopic(statusoppdateringKafkaProps, statusoppdateringEventProcessor)

    private fun initializeDoneWaitingTableProcessor() = PeriodicDoneEventWaitingTableProcessor(donePersistingService, dbMetricsProbe)

    private fun initializePeriodicConsumerPollingCheck() = PeriodicConsumerPollingCheck(this)

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
