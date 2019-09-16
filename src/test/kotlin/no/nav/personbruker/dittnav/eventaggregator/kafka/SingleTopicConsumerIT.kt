package no.nav.personbruker.dittnav.eventaggregator.kafka

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Informasjon
import no.nav.common.JAASCredential
import no.nav.common.KafkaEnvironment
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import no.nav.personbruker.dittnav.eventaggregator.schema.objectmother.InformasjonObjectMother
import no.nav.personbruker.dittnav.eventaggregator.service.impl.SimpleEventCounterService
import no.nav.personbruker.dittnav.eventaggregator.util.KafkaProducerUtil
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldEqualTo
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.junit.jupiter.api.Assertions
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals

object SingleTopicConsumerIT : Spek({

    describe("Skal kunne konsumere Informasjon-eventer som er på avro-format fra Kafka, vha coroutines") {
        val topicen = "singleTopicConsumerTestInformasjon"
        val username = "srvkafkaclient"
        val password = "kafkaclient"
        val embeddedEnv = KafkaEnvironment(
                topicNames = listOf(topicen),
                withSecurity = true,
                withSchemaRegistry = true,
                users = listOf(JAASCredential(username, password))
        )
        val adminClient = embeddedEnv.adminClient

        val env = Environment().copy(
                bootstrapServers = embeddedEnv.brokersURL.substringAfterLast("/"),
                schemaRegistryUrl = embeddedEnv.schemaRegistry!!.url,
                username = username,
                password = password
        )

        val events = (1..10).map { "$it" to InformasjonObjectMother.createInformasjon(it) }.toMap()

        before {
            embeddedEnv.start()
        }

        it("Kafka instansen i minnet har blitt staret") {
            Assertions.assertEquals(embeddedEnv.serverPark.status, KafkaEnvironment.ServerParkStatus.Started)
        }

        it("Produserer noen testeventer") {
            runBlocking {
                KafkaProducerUtil.kafkaAvroProduce(env.bootstrapServers, env.schemaRegistryUrl, topicen, env.username, env.password, events)
            } shouldEqualTo true
        }

        it("Lese inn alle testeventene fra Kafka") {
            val eventProcessor = SimpleEventCounterService<Informasjon>()
            val consumerProps = Kafka.consumerProps(env, "informasjon", true)
            val kafkaConsumer = KafkaConsumer<String, Informasjon>(consumerProps)
            val consumer = Consumer(topicen, kafkaConsumer, eventProcessor)

            runBlocking {
                consumer.poll()

                while (haveAllEventsBeenConsumed(eventProcessor, events)) {
                    delay(100)
                }
                consumer.cancel()

                assertEquals(events.size, eventProcessor.eventCounter)
                eventProcessor.eventCounter
            } `should be equal to` events.size
        }

        after {
            adminClient?.close()
            embeddedEnv.tearDown()
        }
    }

})

private fun haveAllEventsBeenConsumed(eventProcessor: SimpleEventCounterService<Informasjon>, events: Map<String, Informasjon>) =
        eventProcessor.eventCounter < events.size
