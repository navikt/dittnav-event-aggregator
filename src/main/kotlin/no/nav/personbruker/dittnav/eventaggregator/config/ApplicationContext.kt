package no.nav.personbruker.dittnav.eventaggregator.config

import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.PostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.done.CachedDoneEventConsumer
import no.nav.personbruker.dittnav.eventaggregator.done.DoneProducer
import no.nav.personbruker.dittnav.eventaggregator.informasjon.InformasjonProducer
import no.nav.personbruker.dittnav.eventaggregator.melding.MeldingProducer
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveProducer

class ApplicationContext {

    var environment = Environment()
    val database: Database = PostgresDatabase(environment)

    val infoConsumer = KafkaConsumerSetup.setupConsumerForTheInformasjonTopic(environment, database)
    val oppgaveConsumer = KafkaConsumerSetup.setupConsumerForTheOppgaveTopic(environment, database)
    val meldingConsumer = KafkaConsumerSetup.setupConsumerForTheMeldingTopic(environment, database)
    val doneConsumer = KafkaConsumerSetup.setupConsumerForTheDoneTopic(environment, database)

    val cachedDoneEventConsumer = CachedDoneEventConsumer(database)

    val informasjonProducer = InformasjonProducer
    val oppgaveProducer = OppgaveProducer
    val meldingProducer = MeldingProducer
    val doneProducer = DoneProducer

}
