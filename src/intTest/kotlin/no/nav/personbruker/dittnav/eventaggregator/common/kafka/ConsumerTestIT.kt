package no.nav.personbruker.dittnav.eventaggregator.common.kafka

import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.internal.BeskjedIntern
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.personbruker.dittnav.eventaggregator.beskjed.AvroBeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.common.ThrowingEventCounterService
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.RetriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UnretriableDatabaseException
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be greater than`
import org.amshove.kluent.`should contain same`
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class ConsumerTestIT {

    private val topicPartition = TopicPartition("topic", 0)

    private fun consumerMock() = MockConsumer<NokkelIntern, BeskjedIntern>(OffsetResetStrategy.EARLIEST).also {
        it.subscribe(listOf(topicPartition.topic()))
        it.rebalance(listOf(topicPartition))
        it.updateBeginningOffsets(mapOf(topicPartition to 0))
    }

    @Test
    fun `Should attempt process each event exactly once if no exceptions are thrown`() {

        val consumerMock = consumerMock()
        val eventProcessor = ThrowingEventCounterService<BeskjedIntern>()
        val beskjedConsumer = Consumer(topicPartition.topic(), consumerMock, eventProcessor)

        val beskjeder = createEventRecords(10, topicPartition, AvroBeskjedObjectMother::createBeskjed)

        runBlocking {
            beskjedConsumer.startPolling()
            beskjeder.forEach { consumerMock.addRecord(it) }
            delayUntilDone(beskjedConsumer, beskjeder.size)
            beskjedConsumer.stopPolling()
        }

        eventProcessor.successfulEventsCounter `should be equal to` beskjeder.size
        eventProcessor.invocationCounter `should be equal to` beskjeder.size
        eventProcessor.successfulEvents `should contain same` beskjeder.map { it.value() }
    }

    @Test
    @Disabled
    fun `Should attempt to process some events multiple times if a retriable exception was raised`() {
        val consumerMock = MockConsumer<NokkelIntern, BeskjedIntern>(OffsetResetStrategy.EARLIEST).also {
            it.subscribe(listOf(topicPartition.topic()))
            it.rebalance(listOf(topicPartition))
            it.updateBeginningOffsets(mapOf(topicPartition to 0))
        }

        val beskjeder = createEventRecords(3, topicPartition, AvroBeskjedObjectMother::createBeskjed)

        val eventProcessor =
            ThrowingEventCounterService<BeskjedIntern>(RetriableDatabaseException("Transient error"), 2)
        val beskjedConsumer = Consumer(topicPartition.topic(), consumerMock, eventProcessor)

        runBlocking {
            beskjedConsumer.startPolling()

            beskjeder.forEachIndexed { index, consumerRecord ->
                consumerMock.addRecord(consumerRecord)
                delayUntilDone(beskjedConsumer, index + 1)
            }

            beskjedConsumer.stopPolling()
        }

        eventProcessor.successfulEventsCounter `should be equal to` beskjeder.size
        eventProcessor.invocationCounter `should be greater than` beskjeder.size
        eventProcessor.successfulEvents `should contain same` beskjeder.map { it.value() }
    }

    @Test
    fun `Should stop processing events if a non-retriable exception was raised`() {
        val consumerMock = consumerMock()
        val eventProcessor = ThrowingEventCounterService<BeskjedIntern>(UnretriableDatabaseException("Fatal error"), 3)
        val beskjedConsumer = Consumer(topicPartition.topic(), consumerMock, eventProcessor)

        val beskjeder = createEventRecords(5, topicPartition, AvroBeskjedObjectMother::createBeskjed)
        runBlocking {
            beskjedConsumer.startPolling()
            beskjeder.forEach { consumerMock.addRecord(it) }
            delayUntilDone(beskjedConsumer, beskjeder.size)
            beskjedConsumer.stopPolling()
        }

        eventProcessor.successfulEventsCounter `should be equal to` 2
        eventProcessor.invocationCounter `should be equal to` 3
    }
}
