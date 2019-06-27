package no.nav.personbruker.dittnav.eventaggregator

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.config.*
import no.nav.personbruker.dittnav.eventaggregator.config.Config.informasjonTopicName
import no.nav.personbruker.dittnav.eventaggregator.kafka.Consumer

fun main(args: Array<String>) {
    val environment = Environment()

    runBlocking {
        Server.startServer(System.getenv("PORT")?.toInt() ?: 8080).start()

        DatabaseConnectionFactory.initDatabase(environment)

        Flyway.runFlywayMigrations(environment)

        startInformasjonConsumer(environment)
    }

}

private fun startInformasjonConsumer(env: Environment) {
    Consumer.apply {
        create(topics = listOf(informasjonTopicName), kafkaProps = Config.consumerProps(env))
        pollContinuouslyForEvents()
    }
}
