package no.nav.personbruker.dittnav.eventaggregator

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Beskjed
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.common.KafkaEnvironment
import no.nav.personbruker.dittnav.eventaggregator.beskjed.*
import no.nav.personbruker.dittnav.eventaggregator.common.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.kafka.util.KafkaTestUtil
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.Consumer
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.ProducerNameScrubber
import no.nav.personbruker.dittnav.eventaggregator.metrics.StubMetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import org.amshove.kluent.`should equal`
import org.amshove.kluent.shouldEqualTo
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class EndToEndTestIT {

    val database = H2Database()

    val topicen = "endToEndTestItBeskjed"
    val embeddedEnv = KafkaTestUtil.createDefaultKafkaEmbeddedInstance(listOf(topicen))
    val testEnvironment = KafkaTestUtil.createEnvironmentForEmbeddedKafka(embeddedEnv)

    val metricsReporter = StubMetricsReporter()
    val nameScrubber = ProducerNameScrubber("")
    val metricsProbe = EventMetricsProbe(metricsReporter, nameScrubber)

    val adminClient = embeddedEnv.adminClient

    val events = (1..10).map { createNokkel(it) to AvroBeskjedObjectMother.createBeskjed(it) }.toMap()

    init {
        embeddedEnv.start()
    }

    @AfterAll
    fun tearDown() {
        adminClient?.close()
        embeddedEnv.tearDown()

        runBlocking {
            database.dbQuery {
                deleteAllBeskjed()
            }
        }
    }

    @Test
    fun `Kafka instansen i minnet har blitt staret`() {
        embeddedEnv.serverPark.status `should equal` KafkaEnvironment.ServerParkStatus.Started
    }

    @Test
    fun `Skal lese inn Beskjeds-eventer og skrive de til databasen`() {
        `Produserer noen testeventer`()
        `Les inn alle eventene og verifiser at de har blitt lagt til i databasen`()

        runBlocking {
            database.dbQuery {
                getAllBeskjed().size
            } `should equal` events.size
        }
    }

    fun `Produserer noen testeventer`() {
        runBlocking {
            KafkaTestUtil.produceEvents(testEnvironment, topicen, events)
        } shouldEqualTo true
    }

    fun `Les inn alle eventene og verifiser at de har blitt lagt til i databasen`() {
        val beskjedRepository = BeskjedRepository(database)
        val beskjedDatabaseService = BeskjedDatabaseService(beskjedRepository)
        val eventProcessor = BeskjedEventService(beskjedDatabaseService, metricsProbe)
        val consumerProps = Kafka.consumerProps(testEnvironment, EventType.BESKJED, true)
        val kafkaConsumer = KafkaConsumer<Nokkel, Beskjed>(consumerProps)
        val consumer = Consumer(topicen, kafkaConsumer, eventProcessor)

        runBlocking {
            consumer.startPolling()

            `Wait until all events have been written to the database`()

            consumer.stopPolling()
        }
    }

    private fun `Wait until all events have been written to the database`() {
        var currentNumberOfRecords = 0
        while (currentNumberOfRecords < events.size) {
            runBlocking {
                database.dbQuery {
                    currentNumberOfRecords = getAllBeskjed().size
                }
                delay(100)
            }
        }
    }

}
