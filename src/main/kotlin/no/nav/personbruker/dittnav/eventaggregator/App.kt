package no.nav.personbruker.dittnav.eventaggregator

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.skjema.Informasjon

fun main(args: Array<String>) {

    runBlocking {
        Server.startServer(System.getenv("PORT")?.toInt() ?: 8080).start()

        Consumer.apply {
            create(topics = listOf("example.topic.dittnav.informasjon"), kafkaProps = Config.consumerProps(Environment()))
            fetchFromKafka<Informasjon>()
        }

    }

}
