package no.nav.personbruker.dittnav.eventaggregator

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.config.Config
import no.nav.personbruker.dittnav.eventaggregator.config.Config.informasjonTopicName
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.skjema.Informasjon

fun main(args: Array<String>) {

    runBlocking {
        Server.startServer(System.getenv("PORT")?.toInt() ?: 8080).start()
        lesInnProperties()

        startInformasjonConsumer()
    }

}

private fun lesInnProperties() {
    lesInnSecret()
    lesInnIkkeSensitivPropertyDefinertINaisManifiestet()
}

private fun lesInnSecret() {
    val key = "keyForSecret"
    val dummySecretValue: String = System.getenv(key) ?: "ikke_funnet"
    println("Leste inn miljøvariabelen 'keyForSecret' til å være: $dummySecretValue")
}

private fun lesInnIkkeSensitivPropertyDefinertINaisManifiestet() {
    val key = "IKKE_SENSITIV_PROPERTY"
    val propertyValue: String = System.getenv(key) ?: "ikke_funnet"
    println("Leste inn miljøvariabelen IKKE_SENSITIV_PROPERTY til å være: $propertyValue")
}

private fun startInformasjonConsumer() {
    Consumer.apply {
        create(topics = listOf(informasjonTopicName), kafkaProps = Config.consumerProps(Environment()))
        fetchFromKafka<Informasjon>()
    }
}
