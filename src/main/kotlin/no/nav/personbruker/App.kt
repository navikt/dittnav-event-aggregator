package no.nav.personbruker

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.skjema.Melding

fun main(args: Array<String>) {

    runBlocking {
        HealthServer.startServer(System.getenv("PORT")?.toInt() ?: 8080).start()

        Consumer.apply {
            create(topics = listOf("example.topic.dittnav.melding"), kafkaProps = Config.consumerProps(Environment()))
            fetchFromKafka<Melding>()
        }
    }

}
