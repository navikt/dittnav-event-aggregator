package no.nav.personbruker.dittnav.eventaggregator

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.config.Config
import no.nav.personbruker.dittnav.eventaggregator.config.Config.informasjonTopicName
import no.nav.personbruker.dittnav.eventaggregator.config.DatabaseConnectionFactory
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.config.Server
import no.nav.personbruker.dittnav.eventaggregator.kafka.Consumer

fun main(args: Array<String>) {

    runBlocking {
        Server.startServer(System.getenv("PORT")?.toInt() ?: 8080).start()

        DatabaseConnectionFactory.runFlywayMigrations()

        startInformasjonConsumer()
    }

}

private fun startInformasjonConsumer() {
    val currentEnvironment = Environment()
    Consumer.apply {
        create(topics = listOf(informasjonTopicName), kafkaProps = Config.consumerProps(currentEnvironment))
        pollContinuouslyForEvents()
    }
}
