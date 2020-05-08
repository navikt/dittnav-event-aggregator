package no.nav.personbruker.dittnav.eventaggregator.config

import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedEventService
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonPersistingService
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.done.CachedDoneEventConsumer
import no.nav.personbruker.dittnav.eventaggregator.done.DoneEventService
import no.nav.personbruker.dittnav.eventaggregator.done.DoneRepository
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksEventService
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksRepository
import no.nav.personbruker.dittnav.eventaggregator.metrics.buildEventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveEventService
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveRepository

class ApplicationContext {

    var environment = Environment()
    val database: Database = PostgresDatabase(environment)

    val metricsProbe = buildEventMetricsProbe(environment, database)

    val beskjedRepository = BeskjedRepository(database)
    val beskjedPersistingService = BrukernotifikasjonPersistingService(beskjedRepository)
    val beskjedEventProcessor = BeskjedEventService(beskjedPersistingService, metricsProbe)
    val beskjedKafkaProps = Kafka.consumerProps(environment, EventType.BESKJED)
    val beskjedConsumer = KafkaConsumerSetup.setupConsumerForTheBeskjedTopic(beskjedKafkaProps, beskjedEventProcessor)

    val oppgaveRepository = OppgaveRepository(database)
    val oppgavePersistingService = BrukernotifikasjonPersistingService(oppgaveRepository)
    val oppgaveEventProcessor = OppgaveEventService(oppgavePersistingService, metricsProbe)
    val oppgaveKafkaProps = Kafka.consumerProps(environment, EventType.OPPGAVE)
    val oppgaveConsumer = KafkaConsumerSetup.setupConsumerForTheOppgaveTopic(oppgaveKafkaProps, oppgaveEventProcessor)

    val innboksRepository = InnboksRepository(database)
    val innboksPersistingService = BrukernotifikasjonPersistingService(innboksRepository)
    val innboksEventProcessor = InnboksEventService(innboksPersistingService, metricsProbe)
    val innboksKafkaProps = Kafka.consumerProps(environment, EventType.INNBOKS)
    val innboksConsumer = KafkaConsumerSetup.setupConsumerForTheInnboksTopic(innboksKafkaProps, innboksEventProcessor)

    val doneRepository = DoneRepository(database)
    val doneEventProcessor = DoneEventService(doneRepository, metricsProbe)
    val doneKafkaProps = Kafka.consumerProps(environment, EventType.DONE)
    val doneConsumer = KafkaConsumerSetup.setupConsumerForTheDoneTopic(doneKafkaProps, doneEventProcessor)

    val cachedDoneEventConsumer = CachedDoneEventConsumer(doneRepository)
}
