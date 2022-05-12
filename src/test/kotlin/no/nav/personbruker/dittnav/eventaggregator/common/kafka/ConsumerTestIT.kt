package no.nav.personbruker.dittnav.eventaggregator.common.kafka

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import no.nav.brukernotifikasjon.schemas.internal.BeskjedIntern
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.personbruker.dittnav.eventaggregator.beskjed.AvroBeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.common.ThrowingEventCounterService
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.RetriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UnretriableDatabaseException
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Test

class ConsumerTestIT {

    private val topicPartition = TopicPartition("topic", 0)

    private fun mockConsumer() = MockConsumer<NokkelIntern, BeskjedIntern>(OffsetResetStrategy.EARLIEST).also {
        it.subscribe(listOf(topicPartition.topic()))
        it.rebalance(listOf(topicPartition))
        it.updateBeginningOffsets(mapOf(topicPartition to 0))
    }

    @Test
    fun `Should attempt process each event exactly once if no exceptions are thrown`() {

        val consumerMock = mockConsumer()
        val eventProcessor = ThrowingEventCounterService<BeskjedIntern>()
        val beskjedConsumer = Consumer(topicPartition.topic(), consumerMock, eventProcessor)

        val beskjeder = createEventRecords(10, topicPartition, AvroBeskjedObjectMother::createBeskjed)

        runBlocking {
            beskjedConsumer.startPolling()
            beskjeder.forEach { consumerMock.addRecord(it) }
            delayUntilDone(beskjedConsumer, beskjeder.size)
            beskjedConsumer.stopPolling()
        }

        eventProcessor.successfulEventsCounter shouldBe beskjeder.size
        eventProcessor.invocationCounter shouldBe beskjeder.size
        eventProcessor.successfulEvents shouldBe beskjeder.map { it.value() }
    }

    @Test
    fun `Should attempt to process some events multiple times if a retriable exception was raised`() {
        val consumerMock = MockConsumerWithRollbackCheck(1337L).also {
            it.subscribe(listOf(topicPartition.topic()))
            it.rebalance(listOf(topicPartition))
            it.updateBeginningOffsets(mapOf(topicPartition to 0))
        }

        val beskjeder = createEventRecords(3, topicPartition, AvroBeskjedObjectMother::createBeskjed)

        val eventProcessor = ThrowingEventCounterService<BeskjedIntern>(RetriableDatabaseException("Transient error"), 2)
        val beskjedConsumer = Consumer(topicPartition.topic(), consumerMock, eventProcessor)

        shouldNotThrow<TimeoutCancellationException> {
            runBlocking {
                beskjedConsumer.startPolling()

                beskjeder.forEach {
                    consumerMock.addRecord(it)
                }

                withTimeout(1000) {
                    while (!consumerMock.hasRollbacked) {
                        delay(10)
                    }
                }
                beskjedConsumer.stopPolling()
            }
        }
    }

    @Test
    fun `Should stop processing events if a non-retriable exception was raised`() {
        val consumerMock = mockConsumer()
        val eventProcessor = ThrowingEventCounterService<BeskjedIntern>(UnretriableDatabaseException("Fatal error"), 3)
        val beskjedConsumer = Consumer(topicPartition.topic(), consumerMock, eventProcessor)

        val beskjeder = createEventRecords(5, topicPartition, AvroBeskjedObjectMother::createBeskjed)
        runBlocking {
            beskjedConsumer.startPolling()
            beskjeder.forEach { consumerMock.addRecord(it) }
            delayUntilDone(beskjedConsumer, beskjeder.size)
            beskjedConsumer.stopPolling()
        }

        eventProcessor.successfulEventsCounter shouldBe 2
        eventProcessor.invocationCounter shouldBe 3
    }

    private class MockConsumerWithRollbackCheck(var dummyResetOffsetValue: Long) :
        MockConsumer<NokkelIntern, BeskjedIntern>(OffsetResetStrategy.EARLIEST) {
        //MockConsumer har problemer med seek, så må teste på litt hacky måte
        var hasRollbacked = false
        override fun seek(partition: TopicPartition?, offset: Long) {
            if (offset == dummyResetOffsetValue) {
                hasRollbacked = true
                return
            }
            super.seek(partition, offset)
        }

        override fun committed(partitions: MutableSet<TopicPartition>?): MutableMap<TopicPartition, OffsetAndMetadata> {
            return mutableMapOf(partitions!!.first() to OffsetAndMetadata(dummyResetOffsetValue))
        }
    }
}
