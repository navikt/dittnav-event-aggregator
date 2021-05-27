package no.nav.personbruker.dittnav.eventaggregator.common.database.kafka

import kotlinx.coroutines.runBlocking
import no.nav.common.JAASCredential
import no.nav.common.KafkaEnvironment
import no.nav.personbruker.dittnav.eventaggregator.common.database.kafka.util.KafkaConsumerUtil
import no.nav.personbruker.dittnav.eventaggregator.common.database.kafka.util.KafkaProducerUtil
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldContainAll
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

/**
 * Denne testen er kun med for å vise enkel bruk av kafka-embedded-env, en test som publiserer og konsumerer en string.
 * Og en test som publiserer og konsumerer en melding på avro-format.
 * Util-metodene som brukes for å publisere og konsumere er kopiert ut fra en util-klasse i prosjeket kafka-embedded-env.
 * Dette er gjort for å kunne demonstrere hvordan man bruker kafka-embedded-env uten å forholde seg til kafka-properties
 * som er satt opp i dette prosjeket.
 */
class StringBasicKafkaEmbeddedTesting {

    private val topicen = "kafka.topic"
    private val username = "srvkafkaclient"
    private val password = "kafkaclient"
    private val embeddedEnv = KafkaEnvironment(
        topicNames = listOf(topicen),
        withSecurity = false,
        users = listOf(JAASCredential(username, password))
    )
    private val kafkaBrokerUrl = embeddedEnv.brokersURL.substringAfterLast("/")

    private val events = (1..9).map { "$it" to "event$it" }.toMap()

    init {
        embeddedEnv.start()
    }

    @AfterAll
    fun tearDown() {
        embeddedEnv.tearDown()
    }

    @Test
    fun `Kafka-instansen i minnet har blitt staret`() {
        embeddedEnv.serverPark.status `should be equal to` KafkaEnvironment.ServerParkStatus.Started
    }

    @Test
    fun `Skal produsere og konsumere en meldinger som kun er en string`() {
        `produser string-eventer`()
        runBlocking {
            KafkaConsumerUtil.kafkaConsume(
                kafkaBrokerUrl,
                topicen,
                events.size
            )
        } shouldContainAll events
    }

    private fun `produser string-eventer`() {
        runBlocking {
            KafkaProducerUtil.kafkaProduce(
                kafkaBrokerUrl,
                topicen,
                events
            )
        } `should be equal to` true
    }

}
