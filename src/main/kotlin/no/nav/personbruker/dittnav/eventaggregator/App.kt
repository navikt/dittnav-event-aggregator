package no.nav.personbruker.dittnav.eventaggregator

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.event.schemas.Informasjon


fun main(args: Array<String>) {

    runBlocking {
        Server.startServer(System.getenv("PORT")?.toInt() ?: 8080).start()
        lesInnProperties()

        startConsumer()
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

private fun startConsumer() {
    Consumer.apply {
        create(topics = listOf("example.topic.dittnav.informasjon"), kafkaProps = Config.consumerProps(Environment()))
        fetchFromKafka<Informasjon>()
    }
}
