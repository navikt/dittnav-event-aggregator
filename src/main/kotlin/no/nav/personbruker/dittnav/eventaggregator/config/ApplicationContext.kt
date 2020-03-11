package no.nav.personbruker.dittnav.eventaggregator.config

import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.done.CachedDoneEventConsumer

class ApplicationContext {

    var environment = Environment()
    val database: Database = PostgresDatabase(environment)

    val beskjedConsumer = KafkaConsumerSetup.setupConsumerForTheBeskjedTopic(environment, database)
    val oppgaveConsumer = KafkaConsumerSetup.setupConsumerForTheOppgaveTopic(environment, database)
    val innboksConsumer = KafkaConsumerSetup.setupConsumerForTheInnboksTopic(environment, database)
    val doneConsumer = KafkaConsumerSetup.setupConsumerForTheDoneTopic(environment, database)

    val cachedDoneEventConsumer = CachedDoneEventConsumer(database)
}
