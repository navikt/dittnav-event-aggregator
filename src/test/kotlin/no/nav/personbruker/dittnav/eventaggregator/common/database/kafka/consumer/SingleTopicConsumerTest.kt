package no.nav.personbruker.dittnav.eventaggregator.common.database.kafka.consumer

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.internal.BeskjedIntern
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.personbruker.dittnav.eventaggregator.beskjed.AvroBeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.common.SimpleEventCounterService
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.Consumer
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.createBeskjedRecords
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.createEventRecords
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.delayUntilDone
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Test

class SingleTopicConsumerTest {

    @Test
    fun `Lese inn alle testeventene fra Kafka`() {

        val eventProcessor = SimpleEventCounterService<NokkelIntern, BeskjedIntern>()
        val topicPartition = TopicPartition("topic", 0)
        val consumerMock = MockConsumer<NokkelIntern, BeskjedIntern>(OffsetResetStrategy.EARLIEST).also {
            it.subscribe(listOf(topicPartition.topic()))
            it.rebalance(listOf(topicPartition))
            it.updateBeginningOffsets(mapOf(topicPartition to 0))
        }
        val consumer = Consumer(topicPartition.topic(), consumerMock, eventProcessor)

        val events = createBeskjedRecords(10, topicPartition)

        runBlocking {
            consumer.startPolling()

            events.forEach { consumerMock.addRecord(it) }
            delayUntilDone(consumer, events.size)

            consumer.stopPolling()

            eventProcessor.eventCounter
        } shouldBe events.size
    }
}
