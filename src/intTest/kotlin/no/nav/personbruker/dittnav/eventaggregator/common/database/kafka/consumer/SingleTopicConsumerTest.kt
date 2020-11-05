package no.nav.personbruker.dittnav.eventaggregator.common.database.kafka.consumer

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Beskjed
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.common.KafkaEnvironment
import no.nav.personbruker.dittnav.eventaggregator.beskjed.AvroBeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.common.SimpleEventCounterService
import no.nav.personbruker.dittnav.eventaggregator.common.config.KafkaEmbed
import no.nav.personbruker.dittnav.eventaggregator.common.database.kafka.util.KafkaTestUtil
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.Consumer
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import org.amshove.kluent.`should be equal to`
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class SingleTopicConsumerTest {

    private val topicen = "singleTopicConsumerTestBeskjed"
    private val embeddedEnv = KafkaTestUtil.createDefaultKafkaEmbeddedInstance(listOf(topicen))
    private val testEnvironment = KafkaTestUtil.createEnvironmentForEmbeddedKafka(embeddedEnv)

    private val adminClient = embeddedEnv.adminClient

    private val events = (1..10).map { createNokkel(it) to AvroBeskjedObjectMother.createBeskjed(it) }.toMap()

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
        embeddedEnv.serverPark.status `should be equal to` KafkaEnvironment.ServerParkStatus.Started
    }

    @Test
    fun `Lese inn alle testeventene fra Kafka`() {
        `Produserer noen testeventer`()
        val eventProcessor = SimpleEventCounterService<Beskjed>()
        val consumerProps = KafkaEmbed.consumerProps(testEnvironment, EventType.BESKJED, true)
        val kafkaConsumer = KafkaConsumer<Nokkel, Beskjed>(consumerProps)
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
        } `should be equal to` true
    }

    private suspend fun `Vent til alle eventer har blitt konsumert`(eventProcessor: SimpleEventCounterService<Beskjed>) {
        while (`have all events been consumed`(eventProcessor, events)) {
            delay(100)
        }
    }

}

private fun `have all events been consumed`(eventProcessor: SimpleEventCounterService<Beskjed>, events: Map<Nokkel, Beskjed>) =
        eventProcessor.eventCounter < events.size
