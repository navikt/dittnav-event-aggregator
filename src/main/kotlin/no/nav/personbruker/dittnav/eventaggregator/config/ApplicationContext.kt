package no.nav.personbruker.dittnav.eventaggregator.config

import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.done.CachedDoneEventConsumer
import no.nav.personbruker.dittnav.eventaggregator.done.DoneRepository
import no.nav.personbruker.dittnav.eventaggregator.metrics.buildEventMetricsProbe

class ApplicationContext {

    var environment = Environment()
    val database: Database = PostgresDatabase(environment)

    val metricsProbe = buildEventMetricsProbe(environment)
    val doneRepository = DoneRepository(database)

    val beskjedConsumer = KafkaConsumerSetup.setupConsumerForTheBeskjedTopic(environment, database, metricsProbe)
    val oppgaveConsumer = KafkaConsumerSetup.setupConsumerForTheOppgaveTopic(environment, database, metricsProbe)
    val innboksConsumer = KafkaConsumerSetup.setupConsumerForTheInnboksTopic(environment, database, metricsProbe)
    val doneConsumer = KafkaConsumerSetup.setupConsumerForTheDoneTopic(environment, doneRepository, metricsProbe)

    val cachedDoneEventConsumer = CachedDoneEventConsumer(doneRepository)
}
