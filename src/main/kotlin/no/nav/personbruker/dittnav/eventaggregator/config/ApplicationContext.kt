package no.nav.personbruker.dittnav.eventaggregator.config

import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedEventService
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonPersistingService
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.done.DoneEventService
import no.nav.personbruker.dittnav.eventaggregator.done.DoneRepository
import no.nav.personbruker.dittnav.eventaggregator.done.PeriodicDoneEventWaitingTableProcessor
import no.nav.personbruker.dittnav.eventaggregator.health.HealthService
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksEventService
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksRepository
import no.nav.personbruker.dittnav.eventaggregator.metrics.buildDBMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.buildEventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.buildTopicMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.CacheEventCounterService
import no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.KafkaEventCounterService
import no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.KafkaTopicEventCounterService
import no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.topic.TopicEventCounterService
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveEventService
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveRepository
import org.slf4j.LoggerFactory

class ApplicationContext {

    private val log = LoggerFactory.getLogger(ApplicationContext::class.java)

    val environment = Environment()
    val database: Database = PostgresDatabase(environment)

    val eventMetricsProbe = buildEventMetricsProbe(environment, database)
    val dbMetricsProbe = buildDBMetricsProbe(environment, database)

    val beskjedRepository = BeskjedRepository(database)
    val beskjedPersistingService = BrukernotifikasjonPersistingService(beskjedRepository)
    val beskjedEventProcessor = BeskjedEventService(beskjedPersistingService, eventMetricsProbe)
    val beskjedKafkaProps = Kafka.consumerProps(environment, EventType.BESKJED)
    var beskjedConsumer = initializeBeskjedConsumer()

    val oppgaveRepository = OppgaveRepository(database)
    val oppgavePersistingService = BrukernotifikasjonPersistingService(oppgaveRepository)
    val oppgaveEventProcessor = OppgaveEventService(oppgavePersistingService, eventMetricsProbe)
    val oppgaveKafkaProps = Kafka.consumerProps(environment, EventType.OPPGAVE)
    var oppgaveConsumer = initializeOppgaveConsumer()

    val innboksRepository = InnboksRepository(database)
    val innboksPersistingService = BrukernotifikasjonPersistingService(innboksRepository)
    val innboksEventProcessor = InnboksEventService(innboksPersistingService, eventMetricsProbe)
    val innboksKafkaProps = Kafka.consumerProps(environment, EventType.INNBOKS)
    val doneRepository = DoneRepository(database)
    var innboksConsumer = initializeInnboksConsumer()

    val doneEventService = DoneEventService(doneRepository, eventMetricsProbe)
    val doneKafkaProps = Kafka.consumerProps(environment, EventType.DONE)
    var doneConsumer = initializeDoneConsumer()

    var periodicDoneEventWaitingTableProcessor = initializeDoneWaitingTableProcessor()

    val healthService = HealthService(this)

    val kafkaEventCounterService = KafkaEventCounterService(environment)
    val kafkaTopicEventCounterService = KafkaTopicEventCounterService(environment)
    val cacheEventCounterService = CacheEventCounterService(environment, beskjedRepository, innboksRepository, oppgaveRepository, doneRepository)

    val topicMetricsProbe = buildTopicMetricsProbe(environment, database)
    val topicEventCounterService = TopicEventCounterService(environment, topicMetricsProbe)

    private fun initializeBeskjedConsumer() =
            KafkaConsumerSetup.setupConsumerForTheBeskjedTopic(beskjedKafkaProps, beskjedEventProcessor)

    private fun initializeOppgaveConsumer() =
            KafkaConsumerSetup.setupConsumerForTheOppgaveTopic(oppgaveKafkaProps, oppgaveEventProcessor)

    private fun initializeInnboksConsumer() =
            KafkaConsumerSetup.setupConsumerForTheInnboksTopic(innboksKafkaProps, innboksEventProcessor)

    private fun initializeDoneConsumer() = KafkaConsumerSetup.setupConsumerForTheDoneTopic(doneKafkaProps, doneEventService)

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
    }

    private fun initializeDoneWaitingTableProcessor() = PeriodicDoneEventWaitingTableProcessor(doneRepository, dbMetricsProbe)

    fun reinitializeDoneWaitingTableProcessor() {
        if (periodicDoneEventWaitingTableProcessor.isCompleted()) {
            periodicDoneEventWaitingTableProcessor = initializeDoneWaitingTableProcessor()
            log.info("periodicDoneEventWaitingTableProcessor har blitt reinstansiert.")
        } else {
            log.warn("periodicDoneEventWaitingTableProcessor kunne ikke bli reinstansiert fordi den fortsatt er aktiv.")
        }
    }

}
