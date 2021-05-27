package no.nav.personbruker.dittnav.eventaggregator.common.database.kafka

import kotlinx.coroutines.runBlocking
import no.nav.common.KafkaEnvironment
import no.nav.personbruker.dittnav.eventaggregator.beskjed.AvroBeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.common.database.kafka.util.KafkaConsumerUtil
import no.nav.personbruker.dittnav.eventaggregator.common.database.kafka.util.KafkaProducerUtil
import no.nav.personbruker.dittnav.eventaggregator.common.database.kafka.util.KafkaTestUtil
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
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
class AvroBasicKafkaEmbeddedTesting {

    private val topicen = Kafka.beskjedHovedTopicName
    private val embeddedEnv = KafkaTestUtil.createDefaultKafkaEmbeddedInstance(listOf(topicen))

    private val events = (1..9).map { createNokkel(it) to AvroBeskjedObjectMother.createBeskjed(it) }.toMap()

    init {
        embeddedEnv.start()
    }

    @AfterAll
    fun `tear down`() {
        embeddedEnv.tearDown()
    }

    @Test
    fun `Kafka instansen i minnet har blitt staret`() {
        embeddedEnv.serverPark.status `should be equal to` KafkaEnvironment.ServerParkStatus.Started
    }

    @Test
    fun `Lese inn eventer`() {
        `Produser avro-eventer`()
        runBlocking {
            KafkaConsumerUtil.kafkaAvroConsume(embeddedEnv.brokersURL,
                    embeddedEnv.schemaRegistry!!.url,
                    topicen,
                    events.size)
        } shouldContainAll events
    }

    private fun `Produser avro-eventer`() {
        runBlocking {
            KafkaProducerUtil.kafkaAvroProduce(embeddedEnv.brokersURL,
                    embeddedEnv.schemaRegistry!!.url,
                    topicen,
                    events)
        } `should be equal to` true
    }

}
