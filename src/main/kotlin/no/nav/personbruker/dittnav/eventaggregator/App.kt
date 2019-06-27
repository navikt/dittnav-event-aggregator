package no.nav.personbruker.dittnav.eventaggregator

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.config.*
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka.informasjonTopicName
import no.nav.personbruker.dittnav.eventaggregator.kafka.Consumer

fun main(args: Array<String>) {
    val environment = Environment()

    runBlocking {
        Server.startServer(System.getenv("PORT")?.toInt() ?: 8080).start()

        if (databaseFungerKunLokaltForelopig()) {
            Flyway.runFlywayMigrations(environment)

            DatabaseConnectionFactory.initDatabase(environment)
        }

        startInformasjonConsumer(environment)
    }

}

// TODO: Fjern denne metoden når databasen fungerer på Nais
fun databaseFungerKunLokaltForelopig() = !ConfigUtil.isCurrentlyRunningOnNais()

private fun startInformasjonConsumer(env: Environment) {
    Consumer.apply {
        create(topics = listOf(informasjonTopicName), kafkaProps = Kafka.consumerProps(env))
        pollContinuouslyForEvents()
    }
}
