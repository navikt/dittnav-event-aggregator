package no.nav.personbruker.dittnav.eventaggregator.kafka

import kotlinx.coroutines.runBlocking
import no.nav.common.JAASCredential
import no.nav.common.KafkaEnvironment
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import no.nav.personbruker.dittnav.eventaggregator.schema.objectmother.InformasjonObjectMother
import no.nav.personbruker.dittnav.eventaggregator.util.KafkaConsumerUtil
import no.nav.personbruker.dittnav.eventaggregator.util.KafkaProducerUtil
import org.amshove.kluent.shouldContainAll
import org.amshove.kluent.shouldEqualTo
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

/**
 * Denne testen er kun med for å vise enkel bruk av kafka-embedded-env, en test som publiserer og konsumerer en string.
 * Og en test som publiserer og konsumerer en melding på avro-format.
 * Util-metodene som brukes for å publisere og konsumere er kopiert ut fra en util-klasse i prosjeket kafka-embedded-env.
 * Dette er gjort for å kunne demonstrere hvordan man bruker kafka-embedded-env uten å forholde seg til kafka-properties
 * som er satt opp i dette prosjeket.
 */
class AvroBasicKafkaEmbeddedTesting {

    companion object {
        val topicen = Kafka.informasjonTopicName
        val username = "srvkafkaclient"
        val password = "kafkaclient"
        val embeddedEnv = KafkaEnvironment(
                topicNames = listOf(topicen),
                withSecurity = true,
                withSchemaRegistry = true,
                users = listOf(JAASCredential(username, password))
        )

        val events = (1..9).map { "$it" to InformasjonObjectMother.createInformasjon(it) }.toMap()

        @BeforeAll
        @JvmStatic
        fun before() {
            embeddedEnv.start()
        }

        @AfterAll
        @JvmStatic
        fun after() {
            embeddedEnv.tearDown()
        }
    }

    @Test
    fun `Kafka instansen i minnet har blitt staret`() {
        Assertions.assertEquals(embeddedEnv.serverPark.status, KafkaEnvironment.ServerParkStatus.Started)
    }

    @Test
    fun `Lese inn eventer`() {
        `Produser avro-eventer`()
        runBlocking {
            KafkaConsumerUtil.kafkaAvroConsume(embeddedEnv.brokersURL,
                    embeddedEnv.schemaRegistry!!.url,
                    topicen,
                    username,
                    password,
                    events.size)
        } shouldContainAll events
    }

    private fun `Produser avro-eventer`() {
        runBlocking {
            KafkaProducerUtil.kafkaAvroProduce(embeddedEnv.brokersURL,
                    embeddedEnv.schemaRegistry!!.url,
                    topicen,
                    username,
                    password,
                    events)
        } shouldEqualTo true
    }

}
