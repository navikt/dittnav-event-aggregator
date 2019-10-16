package no.nav.personbruker.dittnav.eventaggregator.common.database.kafka.consumer

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Informasjon
import no.nav.common.KafkaEnvironment
import no.nav.personbruker.dittnav.eventaggregator.common.SimpleEventCounterService
import no.nav.personbruker.dittnav.eventaggregator.common.database.kafka.util.KafkaTestUtil
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.Consumer
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import no.nav.personbruker.dittnav.eventaggregator.informasjon.AvroInformasjonObjectMother
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.shouldEqualTo
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class SingleTopicConsumerTest {

    private val topicen = "singleTopicConsumerTestInformasjon"
    private val embeddedEnv = KafkaTestUtil.createDefaultKafkaEmbeddedInstance(listOf(topicen))
    private val testEnvironment = KafkaTestUtil.createEnvironmentForEmbeddedKafka(embeddedEnv)

    private val adminClient = embeddedEnv.adminClient

    private val events = (1..10).map { "$it" to AvroInformasjonObjectMother.createInformasjon(it) }.toMap()

    init {
        embeddedEnv.start()
    }

    @AfterAll
    fun `tear down`() {
        adminClient?.close()
        embeddedEnv.tearDown()
    }

    @Test
    fun `Kafka instansen i minnet har blitt startet`() {
        embeddedEnv.serverPark.status `should equal` KafkaEnvironment.ServerParkStatus.Started
    }

    @Test
    fun `Lese inn alle testeventene fra Kafka`() {
        `Produserer noen testeventer`()
        val eventProcessor = SimpleEventCounterService<Informasjon>()
        val consumerProps = Kafka.consumerProps(testEnvironment, EventType.INFORMASJON, true)
        val kafkaConsumer = KafkaConsumer<String, Informasjon>(consumerProps)
        val consumer = Consumer(topicen, kafkaConsumer, eventProcessor)

        runBlocking {
            consumer.startPolling()

            `Vent til alle eventer har blitt konsumert`(eventProcessor)

            consumer.stopPolling()

            eventProcessor.eventCounter
        } `should be equal to` events.size
    }

    fun `Produserer noen testeventer`() {
        runBlocking {
            KafkaTestUtil.produceEvents(testEnvironment, topicen, events)
        } shouldEqualTo true
    }

    private suspend fun `Vent til alle eventer har blitt konsumert`(eventProcessor: SimpleEventCounterService<Informasjon>) {
        while (`have all events been consumed`(eventProcessor, events)) {
            delay(100)
        }
    }

}

private fun `have all events been consumed`(eventProcessor: SimpleEventCounterService<Informasjon>, events: Map<String, Informasjon>) =
        eventProcessor.eventCounter < events.size
