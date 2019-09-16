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
import org.junit.jupiter.api.Assertions
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

/**
 * Denne testen er kun med for å vise enkel bruk av kafka-embedded-env, en test som publiserer og konsumerer en string.
 * Og en test som publiserer og konsumerer en melding på avro-format.
 * Util-metodene som brukes for å publisere og konsumere er kopiert ut fra en util-klasse i prosjeket kafka-embedded-env.
 * Dette er gjort for å kunne demonstrere hvordan man bruker kafka-embedded-env uten å forholde seg til kafka-properties
 * som er satt opp i dette prosjeket.
 */
object BasicKafkaEmbeddedTestingIT : Spek({

    describe("Skal produsere og konsumere en meldinger som kun er en string") {
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

        before {
            embeddedEnv.start()
        }

        it("Kafka-instansen i minnet har blitt staret") {
            Assertions.assertEquals(embeddedEnv.serverPark.status, KafkaEnvironment.ServerParkStatus.Started)
        }

        it("produsere eventer") {
            runBlocking {
                KafkaProducerUtil.kafkaProduce(kafkaBrokerUrl,
                        topicen,
                        username,
                        password,
                        events)
            } shouldEqualTo true
        }

        it("Lese inn eventer") {
            runBlocking {
                KafkaConsumerUtil.kafkaConsume(kafkaBrokerUrl,
                        topicen,
                        username,
                        password,
                        events.size)
            } shouldContainAll events
        }

        after {
            embeddedEnv.tearDown()
        }
    }


    describe("Skal produsere og konsumere en meldinger som er på Avro-format") {
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

        before {
            embeddedEnv.start()
        }

        it("Kafka instansen i minnet har blitt staret") {
            Assertions.assertEquals(embeddedEnv.serverPark.status, KafkaEnvironment.ServerParkStatus.Started)
        }

        it("produsere eventer") {
            runBlocking {
                KafkaProducerUtil.kafkaAvroProduce(embeddedEnv.brokersURL,
                        embeddedEnv.schemaRegistry!!.url,
                        topicen,
                        username,
                        password,
                        events)
            } shouldEqualTo true
        }

        it("Lese inn eventer") {
            runBlocking {
                KafkaConsumerUtil.kafkaAvroConsume(embeddedEnv.brokersURL,
                        embeddedEnv.schemaRegistry!!.url,
                        topicen,
                        username,
                        password,
                        events.size)
            } shouldContainAll events
        }

        after {
            embeddedEnv.tearDown()
        }
    }

})
