package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka

import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.resetTheGroupIdsOffsetToZero
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.topic.UniqueKafkaEventIdentifierTransformer
import no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.topic.foundRecords
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.errors.InterruptException
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

private val log = LoggerFactory.getLogger(KafkaEventCounterService::class.java)

fun <T> createCountConsumer(eventType: EventType, topic: String, environment: Environment): KafkaConsumer<Nokkel, T> {
    val kafkaProps = Kafka.counterConsumerProps(environment, eventType)
    val consumer = KafkaConsumer<Nokkel, T>(kafkaProps)
    consumer.subscribe(listOf(topic))
    return consumer
}

fun <T> countEvents(consumer: KafkaConsumer<Nokkel, T>, eventType: EventType): Long {
    val start = Instant.now()
    var counter: Long = 0
    var records = consumer.poll(Duration.of(5000, ChronoUnit.MILLIS))
    counter += records.count()

    while (records.foundRecords()) {
        records = consumer.poll(Duration.of(500, ChronoUnit.MILLIS))
        counter += records.count()
    }

    logTimeUsed(start, counter, eventType)
    consumer.resetTheGroupIdsOffsetToZero()
    return counter
}

fun countUniqueEvents(consumer: KafkaConsumer<Nokkel, GenericRecord>, eventType: EventType): Pair<Long, Long> {
    val start = Instant.now()
    var records = consumer.poll(Duration.of(5000, ChronoUnit.MILLIS))
    var duplicationCounter: Long = 0
    val uniqueEvents = HashSet<UniqueKafkaEventIdentifier>(25000000)

    duplicationCounter += countBatch(records, uniqueEvents)

    while (records.foundRecords()) {
        records = consumer.poll(Duration.of(500, ChronoUnit.MILLIS))
        duplicationCounter += countBatch(records, uniqueEvents)
    }

    val numberOfUniqueEvents = uniqueEvents.size.toLong()
    logTimeUsed(start, numberOfUniqueEvents, duplicationCounter, eventType)
    consumer.resetTheGroupIdsOffsetToZero()
    return Pair(numberOfUniqueEvents, duplicationCounter)
}

private fun countBatch(records: ConsumerRecords<Nokkel, GenericRecord>, uniqueEvents: HashSet<UniqueKafkaEventIdentifier>): Int {
    var duplicateCounter = 0
    records.forEach { record ->
        val event = UniqueKafkaEventIdentifierTransformer.toInternal(record)
        val wasNewUniqueEvent = uniqueEvents.add(event)
        if (!wasNewUniqueEvent) {
            duplicateCounter++
        }
    }
    return duplicateCounter
}

fun <T> closeConsumer(consumer: KafkaConsumer<Nokkel, T>) {
    try {
        consumer.close()

    } catch (ie: InterruptException) {
        log.warn("Det skjedde en uventet feil ved lukking av en kafka-counter-consumer", ie)

    } catch (ke: KafkaException) {
        log.warn("Det skjedde en uventet feil ved lukking av en kafka-counter-consumer", ke)

    } catch (e: Exception) {
        log.error("Det skjedde en uventet feil ved lukking av en kafka-counter-consumer", e)
    }
}

fun getDefaultTopicName(eventType: EventType): String {
    return when (eventType) {
        EventType.BESKJED -> Kafka.beskjedTopicName
        EventType.OPPGAVE -> Kafka.oppgaveTopicName
        EventType.INNBOKS -> Kafka.innboksTopicName
        EventType.DONE -> Kafka.doneTopicName
    }
}


private fun logTimeUsed(start: Instant, counter: Long, eventType: EventType) {
    val end = Instant.now()
    val time = end.toEpochMilli() - start.toEpochMilli()
    log.info("Fant $counter $eventType-eventer, det tok ${time}ms")
}

private fun logTimeUsed(start: Instant, counter: Long, uniqueEvents: Long, eventType: EventType) {
    val end = Instant.now()
    val time = end.toEpochMilli() - start.toEpochMilli()
    log.info("Fant $counter unike $eventType-eventer, og $uniqueEvents duplikater, det tok ${time}ms")
}
