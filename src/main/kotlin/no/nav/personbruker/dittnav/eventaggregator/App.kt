package no.nav.personbruker.dittnav.eventaggregator

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.config.Config
import no.nav.personbruker.dittnav.eventaggregator.config.Config.informasjonTopicName
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.skjema.Informasjon

fun main(args: Array<String>) {

    runBlocking {
        Server.startServer(System.getenv("PORT")?.toInt() ?: 8080).start()

        startInformasjonConsumer()
    }

}

private fun startInformasjonConsumer() {
    Consumer.apply {
        create(topics = listOf(informasjonTopicName), kafkaProps = Config.consumerProps(Environment()))
        fetchFromKafka<Informasjon>()
    }
}
