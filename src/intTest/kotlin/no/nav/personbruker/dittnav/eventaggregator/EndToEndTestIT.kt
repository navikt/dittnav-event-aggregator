package no.nav.personbruker.dittnav.eventaggregator

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Informasjon
import no.nav.common.KafkaEnvironment
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import no.nav.personbruker.dittnav.eventaggregator.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.database.entity.getAllInformasjon
import no.nav.personbruker.dittnav.eventaggregator.entity.deleteAllRowsInInformasjon
import no.nav.personbruker.dittnav.eventaggregator.informasjon.InformasjonRepository
import no.nav.personbruker.dittnav.eventaggregator.kafka.Consumer
import no.nav.personbruker.dittnav.eventaggregator.schema.objectmother.InformasjonObjectMother
import no.nav.personbruker.dittnav.eventaggregator.service.impl.InformasjonEventService
import no.nav.personbruker.dittnav.eventaggregator.util.KafkaTestUtil
import org.amshove.kluent.`should equal`
import org.amshove.kluent.shouldEqualTo
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class EndToEndTestIT {

    val database = H2Database()

    val topicen = "endToEndTestItInformasjon"
    val embeddedEnv = KafkaTestUtil.createDefaultKafkaEmbeddedInstance(listOf(topicen))
    val testEnvironment = KafkaTestUtil.createEnvironmentForEmbeddedKafka(embeddedEnv)

    val adminClient = embeddedEnv.adminClient

    val events = (1..10).map { "$it" to InformasjonObjectMother.createInformasjon(it) }.toMap()

    init {
        embeddedEnv.start()
    }

    @AfterAll
    fun tearDown() {
        adminClient?.close()
        embeddedEnv.tearDown()

        runBlocking {
            database.dbQuery {
                deleteAllRowsInInformasjon()
            }
        }
    }

    @Test
    fun `Kafka instansen i minnet har blitt staret`() {
        embeddedEnv.serverPark.status `should equal` KafkaEnvironment.ServerParkStatus.Started
    }

    @Test
    fun `Skal lese inn informasjons-eventer og skrive de til databasen`() {
        `Produserer noen testeventer`()
        `Les inn alle eventene og verifiser at de har blitt lagt til i databasen`()

        runBlocking {
            database.dbQuery {
                getAllInformasjon().size
            } `should equal` events.size
        }
    }

    fun `Produserer noen testeventer`() {
        runBlocking {
            KafkaTestUtil.produceEvents(testEnvironment, topicen, events)
        } shouldEqualTo true
    }

    fun `Les inn alle eventene og verifiser at de har blitt lagt til i databasen`() {
        val informasjonRepository = InformasjonRepository(database)
        val eventProcessor = InformasjonEventService(informasjonRepository)
        val consumerProps = Kafka.consumerProps(testEnvironment, "informasjon", true)
        val kafkaConsumer = KafkaConsumer<String, Informasjon>(consumerProps)
        val consumer = Consumer(topicen, kafkaConsumer, eventProcessor)

        runBlocking {
            consumer.poll()

            `Wait until all events have been written to the database`()

            consumer.cancel()
        }
    }

    private fun `Wait until all events have been written to the database`() {
        var currentNumberOfRecords = 0
        while (currentNumberOfRecords < events.size) {
            runBlocking {
                database.dbQuery {
                    currentNumberOfRecords = getAllInformasjon().size
                }
                delay(100)
            }
        }
    }

}
