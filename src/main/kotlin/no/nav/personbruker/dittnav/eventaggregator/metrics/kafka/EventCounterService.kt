package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka

import no.nav.brukernotifikasjon.schemas.*
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.resetTheGroupIdsOffsetToZero
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.PartitionInfo
import org.apache.kafka.common.errors.InterruptException
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

class EventCounterService(val environment: Environment) {

    private val log = LoggerFactory.getLogger(EventCounterService::class.java)

    private val beskjedConsumer = createCountConsumer<Beskjed>(EventType.BESKJED, Kafka.beskjedTopicName)
    private val oppgaveConsumer = createCountConsumer<Oppgave>(EventType.OPPGAVE, Kafka.oppgaveTopicName)
    private val innboksConsumer = createCountConsumer<Innboks>(EventType.INNBOKS, Kafka.innboksTopicName)
    private val doneConsumer = createCountConsumer<Done>(EventType.DONE, Kafka.doneTopicName)

    private fun <T> createCountConsumer(eventType: EventType, topic: String): KafkaConsumer<Nokkel, T> {
        val kafkaProps = Kafka.counterConsumerProps(environment, eventType)
        val consumer = KafkaConsumer<Nokkel, T>(kafkaProps)
        consumer.subscribe(listOf(topic))
        return consumer
    }

    fun countAllEvents(): NumberOfRecords {
        val result = NumberOfRecords(
                beskjed = countBeskjeder(),
                innboks = countInnboksEventer(),
                oppgaver = countOppgaver(),
                done = countDoneEvents()
        )

        log.info("Fant følgende eventer:")
        log.info(result.toString())
        return result
    }

    fun countBeskjeder(): Long {
        return try {
            countEvents(beskjedConsumer)

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall beskjed-eventer", e)
            -1
        }
    }

    fun countInnboksEventer(): Long {
        return try {
            countEvents(innboksConsumer)

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall innboks-eventer", e)
            -1
        }
    }

    fun countOppgaver(): Long {
        return try {
            countEvents(oppgaveConsumer)

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall oppgave-eventer", e)
            -1
        }
    }

    fun countDoneEvents(): Long {
        return try {
            countEvents(doneConsumer)

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall done-eventer", e)
            -1
        }
    }

    private fun <T> countEvents(consumer: KafkaConsumer<Nokkel, T>): Long {
        val start = Instant.now()
        var counter: Long = 0
        var records = consumer.poll(Duration.of(5000, ChronoUnit.MILLIS))
        counter += records.count()

        while (records.foundRecords()) {
            records = consumer.poll(Duration.of(500, ChronoUnit.MILLIS))
            counter += records.count()
        }

        logTimeUsed(start, counter, consumer.listTopics())
        consumer.resetTheGroupIdsOffsetToZero()
        return counter
    }

    fun <T> ConsumerRecords<Nokkel, T>.foundRecords(): Boolean {
        return !isEmpty
    }

    private fun logTimeUsed(start: Instant, counter: Long, consumersTopics: MutableMap<String, MutableList<PartitionInfo>>) {
        val topicName = consumersTopics.values.first()[0].topic()
        val end = Instant.now()
        val time = end.toEpochMilli() - start.toEpochMilli()
        log.info("Fant $counter eventer, på topic-en $topicName, det tok ${time}ms")
    }

    fun closeAllConsumers() {
        closeConsumer(beskjedConsumer)
        closeConsumer(innboksConsumer)
        closeConsumer(oppgaveConsumer)
        closeConsumer(doneConsumer)
    }

    private fun <T> closeConsumer(consumer: KafkaConsumer<Nokkel, T>) {
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

}
