package no.nav.personbruker.dittnav.eventaggregator.kafka

import kotlinx.coroutines.runBlocking
import no.nav.common.JAASCredential
import no.nav.common.KafkaEnvironment
import no.nav.personbruker.dittnav.eventaggregator.util.KafkaConsumerUtil
import no.nav.personbruker.dittnav.eventaggregator.util.KafkaProducerUtil
import org.amshove.kluent.`should equal`
import org.amshove.kluent.shouldContainAll
import org.amshove.kluent.shouldEqualTo
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

    val topicen = "kafka.topic"
    val username = "srvkafkaclient"
    val password = "kafkaclient"
    val embeddedEnv = KafkaEnvironment(
            topicNames = listOf(topicen),
            withSecurity = true,
            users = listOf(JAASCredential(username, password))
    )
    val kafkaBrokerUrl = embeddedEnv.brokersURL.substringAfterLast("/")

    val events = (1..9).map { "$it" to "event$it" }.toMap()

    init {
        embeddedEnv.start()
    }

    @AfterAll
    fun tearDown() {
        embeddedEnv.tearDown()
    }

    @Test
    fun `Kafka-instansen i minnet har blitt staret`() {
        embeddedEnv.serverPark.status `should equal` KafkaEnvironment.ServerParkStatus.Started
    }

    @Test
    fun `Skal produsere og konsumere en meldinger som kun er en string`() {
        `produser string-eventer`()
        runBlocking {
            KafkaConsumerUtil.kafkaConsume(kafkaBrokerUrl,
                    topicen,
                    username,
                    password,
                    events.size)
        } shouldContainAll events
    }

    private fun `produser string-eventer`() {
        runBlocking {
            KafkaProducerUtil.kafkaProduce(kafkaBrokerUrl,
                    topicen,
                    username,
                    password,
                    events)
        } shouldEqualTo true
    }

}
