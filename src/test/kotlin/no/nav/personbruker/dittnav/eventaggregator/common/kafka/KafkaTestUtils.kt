package no.nav.personbruker.dittnav.eventaggregator.common.kafka

import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.personbruker.dittnav.eventaggregator.common.SimpleEventCounterService
import no.nav.personbruker.dittnav.eventaggregator.common.ThrowingEventCounterService
import no.nav.personbruker.dittnav.eventaggregator.nokkel.AvroNokkelInternObjectMother
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.common.TopicPartition

internal suspend fun <K, V> delayUntilCommittedOffset(
    consumer: MockConsumer<K, V>,
    partition: TopicPartition,
    offset: Long
) {
    withTimeout(1000) {
        while ((consumer.committed(setOf(partition))[partition]?.offset() ?: 0) < offset) {
            delay(10)
        }
    }
}

internal suspend fun <V> delayUntilDone(consumer: Consumer<V>, numberOfEvents: Int) {
    withTimeout(1000) {
        while (getProcessedCount(consumer) < numberOfEvents && consumer.job.isActive) {
            delay(10)
        }
    }
}

private fun <V> getProcessedCount(consumer: Consumer<V>): Int {
    val processor = consumer.eventBatchProcessorService

    return when (processor) {
        is SimpleEventCounterService<*> -> processor.eventCounter
        is ThrowingEventCounterService<*> -> processor.successfulEventsCounter
        else -> 0
    }
}

internal fun <V> createEventRecords(
    number: Int,
    partition: TopicPartition,
    eventCreator: (offset: Int) -> V
): List<ConsumerRecord<NokkelIntern, V>> {
    return (0 until number).map { offset ->
        ConsumerRecord(
            partition.topic(),
            partition.partition(),
            offset.toLong(),
            AvroNokkelInternObjectMother.createNokkelWithEventId(offset),
            eventCreator(offset)
        )
    }
}

internal fun <V> createEventRecords(
    number: Int,
    partition: TopicPartition,
    eventCreator: () -> V
): List<ConsumerRecord<NokkelIntern, V>> {
    return (0 until number).map { offset ->
        ConsumerRecord(
            partition.topic(),
            partition.partition(),
            offset.toLong(),
            AvroNokkelInternObjectMother.createNokkelWithEventId(offset),
            eventCreator()
        )
    }
}
