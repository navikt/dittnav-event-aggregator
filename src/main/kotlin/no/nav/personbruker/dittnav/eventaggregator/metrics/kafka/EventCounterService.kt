package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka

import no.nav.brukernotifikasjon.schemas.*
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

class EventCounterService(environment: Environment) {

    private var environmentWithCounterGroupId: Environment = environment.copy(groupId = "eventCounter004")

    private val log = LoggerFactory.getLogger(EventCounterService::class.java)

    fun countEvents(): NumberOfRecords {
        val result = NumberOfRecords()
        try {
            result.beskjed = countEventsForTopic<Beskjed>(EventType.BESKJED, Kafka.beskjedTopicName)
            result.innboks = countEventsForTopic<Innboks>(EventType.INNBOKS, Kafka.innboksTopicName)
            result.oppgaver = countEventsForTopic<Oppgave>(EventType.OPPGAVE, Kafka.oppgaveTopicName)
            result.done = countEventsForTopic<Done>(EventType.DONE, Kafka.doneTopicName)

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall evener", e)
        }

        log.info("Fant følgende eventer:")
        log.info(result.toString())
        return result
    }

    private fun <T> countEventsForTopic(eventType: EventType, topic: String): Long {
        var counter: Long = 0
        createCountConsumer<T>(eventType).use { consumer ->
            counter = countEvents(consumer, topic)
        }
        return counter
    }

    private fun <T> createCountConsumer(eventType: EventType): KafkaConsumer<Nokkel, T> {
        val kafkaProps = Kafka.consumerProps(environmentWithCounterGroupId, eventType)
        return KafkaConsumer(kafkaProps)
    }

    private fun <T> countEvents(consumer: KafkaConsumer<Nokkel, T>, topic: String): Long {
        consumer.subscribe(listOf(topic))

        val start = Instant.now()
        var counter: Long = 0
        var records = consumer.poll(Duration.of(1000, ChronoUnit.MILLIS))
        counter += records.count()

        while (foundRecords(records)) {
            records = consumer.poll(Duration.of(500, ChronoUnit.MILLIS))
            counter += records.count()
        }
        logTimeUsed(start, counter, topic)
        return counter
    }

    private fun <T> foundRecords(records: ConsumerRecords<Nokkel, T>) =
            !records.isEmpty

    private fun logTimeUsed(start: Instant, counter: Long, topic: String) {
        val end = Instant.now()
        val time = end.toEpochMilli() - start.toEpochMilli()
        log.info("Fant $counter eventer, på topic-en $topic, det tok ${time}ms")
    }

}
