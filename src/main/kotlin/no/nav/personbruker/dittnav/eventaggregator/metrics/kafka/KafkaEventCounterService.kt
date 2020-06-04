package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka

import no.nav.brukernotifikasjon.schemas.*
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.resetTheGroupIdsOffsetToZero
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.Kafka
import no.nav.personbruker.dittnav.eventaggregator.config.isOtherEnvironmentThanProd
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.errors.InterruptException
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

class KafkaEventCounterService(val environment: Environment) {

    private val log = LoggerFactory.getLogger(KafkaEventCounterService::class.java)

    private var beskjedConsumer = createCountConsumer<Beskjed>(EventType.BESKJED, Kafka.beskjedTopicName)
    private var oppgaveConsumer = createCountConsumer<Oppgave>(EventType.OPPGAVE, Kafka.oppgaveTopicName)
    private var innboksConsumer = createCountConsumer<Innboks>(EventType.INNBOKS, Kafka.innboksTopicName)
    private var doneConsumer = createCountConsumer<Done>(EventType.DONE, Kafka.doneTopicName)

    private fun <T> createCountConsumer(eventType: EventType, topic: String): KafkaConsumer<Nokkel, T> {
        val kafkaProps = Kafka.counterConsumerProps(environment, eventType)
        val consumer = KafkaConsumer<Nokkel, T>(kafkaProps)
        consumer.subscribe(listOf(topic))
        return consumer
    }

    fun countAllEvents(): NumberOfKafkaRecords {
        val result = NumberOfKafkaRecords(
                beskjed = countBeskjeder(),
                innboks = countInnboksEventer(),
                oppgaver = countOppgaver(),
                done = countDoneEvents()
        )

        log.info("Fant følgende eventer:\n$result")
        return result
    }

    fun countBeskjeder(topicToCount: String? = null): Long {
        return try {
            if(!topicToCount.isNullOrBlank()) {
                beskjedConsumer = createCountConsumer(EventType.BESKJED, topicToCount)
            }
            countEvents(beskjedConsumer, EventType.BESKJED)

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall beskjed-eventer", e)
            -1
        }
    }

    fun countInnboksEventer(topicToCount: String? = null): Long {
        return if (isOtherEnvironmentThanProd()) {
            try {
                if(!topicToCount.isNullOrBlank()) {
                    innboksConsumer = createCountConsumer(EventType.INNBOKS, topicToCount)
                }
                countEvents(innboksConsumer, EventType.INNBOKS)
            } catch (e: Exception) {
                log.warn("Klarte ikke å telle antall innboks-eventer", e)
                -1L
            }
        } else {
            0
        }
    }

    fun countOppgaver(topicToCount: String? = null): Long {
        return try {
            if(!topicToCount.isNullOrBlank()) {
                oppgaveConsumer = createCountConsumer(EventType.OPPGAVE, topicToCount)
            }
            countEvents(oppgaveConsumer, EventType.OPPGAVE)

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall oppgave-eventer", e)
            -1
        }
    }

    fun countDoneEvents(topicToCount: String? = null): Long {
        return try {
            if(!topicToCount.isNullOrBlank()) {
                doneConsumer = createCountConsumer(EventType.DONE, topicToCount)
            }
            countEvents(doneConsumer, EventType.DONE)

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall done-eventer", e)
            -1
        }
    }

    private fun <T> countEvents(consumer: KafkaConsumer<Nokkel, T>, eventType: EventType): Long {
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

    fun <T> ConsumerRecords<Nokkel, T>.foundRecords(): Boolean {
        return !isEmpty
    }

    private fun logTimeUsed(start: Instant, counter: Long, eventType: EventType) {
        val end = Instant.now()
        val time = end.toEpochMilli() - start.toEpochMilli()
        log.info("Fant $counter $eventType-eventer, det tok ${time}ms")
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
