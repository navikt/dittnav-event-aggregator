package no.nav.personbruker.dittnav.eventaggregator

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.config.*
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka.informasjonTopicName
import no.nav.personbruker.dittnav.eventaggregator.kafka.Consumer
import java.sql.Connection

fun main() {

    val environment = Environment()

    runBlocking {
        Server.configure().start()
        Flyway.runFlywayMigrations(environment)
        val database = Database(environment)
        startInformasjonConsumer(environment, database)
    }
}

private fun startInformasjonConsumer(env: Environment, database: Database) {
    Consumer.apply {
        create(topics = listOf(informasjonTopicName), kafkaProps = Kafka.consumerProps(env), database = database)
        pollContinuouslyForEvents()
    }
}
