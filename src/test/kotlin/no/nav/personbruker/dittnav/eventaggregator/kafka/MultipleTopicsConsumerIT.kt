package no.nav.personbruker.dittnav.eventaggregator.kafka

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Informasjon
import no.nav.brukernotifikasjon.schemas.Melding
import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.common.JAASCredential
import no.nav.common.KafkaEnvironment
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import no.nav.personbruker.dittnav.eventaggregator.config.KafkaConsumerSetup.setupConsumerForTheInformasjonTopic
import no.nav.personbruker.dittnav.eventaggregator.config.KafkaConsumerSetup.setupConsumerForTheMeldingTopic
import no.nav.personbruker.dittnav.eventaggregator.config.KafkaConsumerSetup.setupConsumerForTheOppgaveTopic
import no.nav.personbruker.dittnav.eventaggregator.schema.objectmother.InformasjonObjectMother
import no.nav.personbruker.dittnav.eventaggregator.schema.objectmother.MeldingObjectMother
import no.nav.personbruker.dittnav.eventaggregator.schema.objectmother.OppgaveObjectMother
import no.nav.personbruker.dittnav.eventaggregator.service.impl.SimpleEventCounterService
import no.nav.personbruker.dittnav.eventaggregator.util.KafkaProducerUtil
import org.amshove.kluent.shouldEqualTo
import org.apache.avro.generic.GenericRecord
import org.junit.jupiter.api.Assertions
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals

object MultipleTopicsConsumerIT : Spek({

    val informasjonEventProcessor = SimpleEventCounterService<Informasjon>()
    val oppgaveEventProcessor = SimpleEventCounterService<Oppgave>()
    val meldingEventProcessor = SimpleEventCounterService<Melding>()

    val informasjonEvents = (1..10).map { "$it" to InformasjonObjectMother.createInformasjon(it) }.toMap()
    val oppgaveEvents = (1..10).map { "$it" to OppgaveObjectMother.createOppgave(it) }.toMap()
    val meldingEvents = (1..10).map { "$it" to MeldingObjectMother.createMelding(it) }.toMap()

    describe("Skal kunne konsumere fra flere topics i parallell") {
        val informasjonTopic = Kafka.informasjonTopicName
        val oppgaveTopic = Kafka.oppgaveTopicName
        val meldingTopic = Kafka.meldingTopicName
        val username = "srvkafkaclient"
        val password = "kafkaclient"
        val embeddedEnv = KafkaEnvironment(
                topicNames = listOf(informasjonTopic, oppgaveTopic, meldingTopic),
                withSecurity = true,
                withSchemaRegistry = true,
                users = listOf(JAASCredential(username, password))
        )
        val adminClient = embeddedEnv.adminClient

        val env1 = Environment().copy(
            bootstrapServers = embeddedEnv.brokersURL.substringAfterLast("/"),
            schemaRegistryUrl = embeddedEnv.schemaRegistry!!.url,
            username = username,
            password = password
    )
        val env = env1

        before {
            embeddedEnv.start()
        }

        it("Kafka instansen i minnet har blitt staret") {
            Assertions.assertEquals(embeddedEnv.serverPark.status, KafkaEnvironment.ServerParkStatus.Started)
        }

        it("Produserer testeventer for alle topics") {
            runBlocking {
                val producedAllInformasjon = produceEvents(env, informasjonTopic, informasjonEvents)
                val producedAllOppgave = produceEvents(env, oppgaveTopic, oppgaveEvents)
                val producedAllMelding = produceEvents(env, meldingTopic, meldingEvents)
                producedAllInformasjon && producedAllOppgave && producedAllMelding
            } shouldEqualTo true
        }

        it("Lese inn alle testeventene fra alle topic-ene på Kafka") {
            val informasjonConsumer = createInfoConsumer(env, informasjonEventProcessor)
            val oppgaveConsumer = createOppgaveConsumer(env, oppgaveEventProcessor)
            val meldingConsumer = createMeldingConsumer(env, meldingEventProcessor)

            runBlocking {
                informasjonConsumer.poll()
                oppgaveConsumer.poll()
                meldingConsumer.poll()

                while (harAlleEventerBlittLest(informasjonEventProcessor, informasjonEvents,
                                oppgaveEventProcessor, oppgaveEvents,
                                meldingEventProcessor, meldingEvents)) {
                    delay(100)
                }
                informasjonConsumer.cancel()
                oppgaveConsumer.cancel()
                meldingConsumer.cancel()

                assertEquals(informasjonEvents.size, informasjonEventProcessor.eventCounter)
                assertEquals(meldingEvents.size, meldingEventProcessor.eventCounter)
                assertEquals(meldingEvents.size, meldingEventProcessor.eventCounter)
            }
        }

        after {
            adminClient?.close()
            embeddedEnv.tearDown()
        }
    }

})

private suspend fun produceEvents(env: Environment, informasjonTopic: String, informasjonEvents: Map<String, GenericRecord>): Boolean {
    val producedAllInformasjon = KafkaProducerUtil.kafkaAvroProduce(
            env.bootstrapServers,
            env.schemaRegistryUrl,
            informasjonTopic,
            env.username,
            env.password,
            informasjonEvents)
    return producedAllInformasjon
}

private fun createInfoConsumer(env: Environment, informasjonEventProcessor: SimpleEventCounterService<Informasjon>): Consumer<Informasjon> {
    val kafkaProps = Kafka.consumerProps(env, "informasjon", true)
    return setupConsumerForTheInformasjonTopic(kafkaProps, informasjonEventProcessor)
}

private fun createOppgaveConsumer(env: Environment, oppgaveEventProcessor: SimpleEventCounterService<Oppgave>): Consumer<Oppgave> {
    val kafkaProps = Kafka.consumerProps(env, "oppgave", true)
    return setupConsumerForTheOppgaveTopic(kafkaProps, oppgaveEventProcessor)
}

private fun createMeldingConsumer(env: Environment, meldingEventProcessor: SimpleEventCounterService<Melding>): Consumer<Melding> {
    val kafkaProps = Kafka.consumerProps(env, "melding", true)
    return setupConsumerForTheMeldingTopic(kafkaProps, meldingEventProcessor)
}

private fun harAlleEventerBlittLest(informasjon: SimpleEventCounterService<Informasjon>, informasjonEvents: Map<String, Informasjon>,
                                    oppgave: SimpleEventCounterService<Oppgave>, oppgaveEvents: Map<String, Oppgave>,
                                    melding: SimpleEventCounterService<Melding>, meldingEvents: Map<String, Melding>): Boolean {
    return informasjon.eventCounter < informasjonEvents.size &&
            oppgave.eventCounter < oppgaveEvents.size &&
            melding.eventCounter < meldingEvents.size
}
