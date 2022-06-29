package no.nav.personbruker.dittnav.eventaggregator.common.kafka

import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import no.nav.brukernotifikasjon.schemas.internal.*
import no.nav.personbruker.dittnav.eventaggregator.beskjed.AvroBeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.common.SimpleEventCounterService
import no.nav.personbruker.dittnav.eventaggregator.common.ThrowingEventCounterService
import no.nav.personbruker.dittnav.eventaggregator.done.schema.AvroDoneObjectMother
import no.nav.personbruker.dittnav.eventaggregator.innboks.AvroInnboksObjectMother
import no.nav.personbruker.dittnav.eventaggregator.nokkel.AvroNokkelInternObjectMother
import no.nav.personbruker.dittnav.eventaggregator.oppgave.AvroOppgaveObjectMother
import no.nav.personbruker.dittnav.eventaggregator.statusoppdatering.AvroStatusoppdateringObjectMother
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

internal suspend fun <K, V> delayUntilDone(consumer: Consumer<K, V>, numberOfEvents: Int) {
    withTimeout(1000) {
        while (getProcessedCount(consumer) < numberOfEvents && consumer.job.isActive) {
            delay(10)
        }
    }
}

private fun <K, V> getProcessedCount(consumer: Consumer<K, V>): Int {
    val processor = consumer.eventBatchProcessorService

    return when (processor) {
        is SimpleEventCounterService<*, *> -> processor.eventCounter
        is ThrowingEventCounterService<*> -> processor.successfulEventsCounter
        else -> 0
    }
}

internal fun createBeskjedRecords(number: Int, partition: TopicPartition): List<ConsumerRecord<NokkelIntern, BeskjedIntern>> =
    createEventRecords(number, partition, AvroNokkelInternObjectMother::createNokkelWithEventId, AvroBeskjedObjectMother::createBeskjed)

internal fun createOppgaveRecords(number: Int, partition: TopicPartition): List<ConsumerRecord<NokkelIntern, OppgaveIntern>> =
    createEventRecords(number, partition, AvroNokkelInternObjectMother::createNokkelWithEventId, AvroOppgaveObjectMother::createOppgave)

internal fun createInnboksRecords(number: Int, partition: TopicPartition): List<ConsumerRecord<NokkelIntern, InnboksIntern>> =
    createEventRecords(number, partition, AvroNokkelInternObjectMother::createNokkelWithEventId, AvroInnboksObjectMother::createInnboks)

internal fun createStatusoppdateringRecords(number: Int, partition: TopicPartition): List<ConsumerRecord<NokkelIntern, StatusoppdateringIntern>> =
    createEventRecords(number, partition, AvroNokkelInternObjectMother::createNokkelWithEventId, AvroStatusoppdateringObjectMother::createStatusoppdatering)

internal fun createDoneRecords(number: Int, partition: TopicPartition): List<ConsumerRecord<NokkelIntern, DoneIntern>> =
    createEventRecords(number, partition, AvroNokkelInternObjectMother::createNokkelWithEventId, AvroDoneObjectMother::createDone)

internal fun <K, V> createEventRecords(
    number: Int,
    partition: TopicPartition,
    keyCreator: (Int) -> K,
    eventCreator: (Int) -> V
): List<ConsumerRecord<K, V>> {
    return (0 until number).map { offset ->
        ConsumerRecord(
            partition.topic(),
            partition.partition(),
            offset.toLong(),
            keyCreator(offset),
            eventCreator(offset)
        )
    }
}

internal fun <K, V> createEventRecords(
    number: Int,
    partition: TopicPartition,
    keyCreator: (Int) -> K,
    eventCreator: () -> V
): List<ConsumerRecord<K, V>> {
    return (0 until number).map { offset ->
        ConsumerRecord(
            partition.topic(),
            partition.partition(),
            offset.toLong(),
            keyCreator(offset),
            eventCreator()
        )
    }
}
