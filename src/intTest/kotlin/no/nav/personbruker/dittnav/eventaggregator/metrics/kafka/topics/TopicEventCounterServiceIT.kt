package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.topics

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.beskjed.AvroBeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.common.database.kafka.util.KafkaTestUtil
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.topic.TopicEventCounterService
import no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.topic.TopicMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.topic.TopicMetricsSession
import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldEqualTo
import org.apache.avro.generic.GenericRecord
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class TopicEventCounterServiceIT {

    private val topicen = "topicEventCounterServiceIT"
    private val embeddedEnv = KafkaTestUtil.createDefaultKafkaEmbeddedInstance(listOf(topicen))
    private val testEnvironment = KafkaTestUtil.createEnvironmentForEmbeddedKafka(embeddedEnv)

    private val adminClient = embeddedEnv.adminClient

    private val events = (1..5).map { createNokkel(it) to AvroBeskjedObjectMother.createBeskjed(it) }.toMap()

    init {
        embeddedEnv.start()
    }

    @AfterAll
    fun `tear down`() {
        adminClient?.close()
        embeddedEnv.tearDown()
    }

    @Test
    fun `Skal telle korrekt totalantall av eventer og gruppere de som er unike og duplikater`() {
        `Produser det samme settet av eventer tre ganger`()

        val metricsSession = TopicMetricsSession(EventType.BESKJED)
        val metricsProbe = mockk<TopicMetricsProbe>(relaxed = true)
        `Sorg for at metrics session trigges`(metricsProbe, metricsSession)

        val service = TopicEventCounterService(testEnvironment, metricsProbe)
        val countConsumer = service.createCountConsumer<GenericRecord>(EventType.BESKJED, topicen, testEnvironment, true)

        runBlocking {
            service.countEventsAndReportMetrics(countConsumer, EventType.BESKJED)
        }

        metricsSession.getDuplicates() `should be equal to` events.size * 2
        metricsSession.getTotalNumber() `should be equal to` events.size * 3
        metricsSession.getNumberOfUniqueEvents() `should be equal to` events.size
    }

    private fun `Produser det samme settet av eventer tre ganger`() {
        runBlocking {
            val fikkProduserBatch1 = KafkaTestUtil.produceEvents(testEnvironment, topicen, events)
            val fikkProduserBatch2 = KafkaTestUtil.produceEvents(testEnvironment, topicen, events)
            val fikkProduserBatch3 = KafkaTestUtil.produceEvents(testEnvironment, topicen, events)
            fikkProduserBatch1 && fikkProduserBatch2 && fikkProduserBatch3
        } shouldEqualTo true
    }

    private fun `Sorg for at metrics session trigges`(metricsProbe: TopicMetricsProbe, metricsSession: TopicMetricsSession) {
        val slot = slot<suspend TopicMetricsSession.() -> Unit>()
        coEvery { metricsProbe.runWithMetrics(any(), capture(slot)) } coAnswers {
            slot.captured.invoke(metricsSession)
        }
    }

}
