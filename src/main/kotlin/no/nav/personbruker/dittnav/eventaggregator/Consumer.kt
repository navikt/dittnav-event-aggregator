package no.nav.personbruker.dittnav.eventaggregator

import io.prometheus.client.Counter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.RetriableException
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.coroutines.CoroutineContext

object Consumer : CoroutineScope {

    lateinit var job: Job
    lateinit var topics: List<String>
    lateinit var kafkaProps: Properties

    val log = LoggerFactory.getLogger(Consumer::class.java)

    val MESSAGES_SEEN = Counter.build()
            .name("messages_seen")
            .namespace("dittnav_consumer")
            .help("Messages read since last startup")
            .labelNames("topic", "partition")
            .register()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    fun cancel() {
        job.cancel()
    }

    fun isRunning() = job.isActive

    fun create(topics: List<String>, kafkaProps: Properties) {
        job = Job()
        Consumer.kafkaProps = kafkaProps
        Consumer.topics = topics
    }


    fun <T> pollContinuouslyForEvents() {
        KafkaConsumer<String, T>(kafkaProps).use { consumer ->
            consumer.subscribe(topics)

            while (job.isActive) {
                processBatchOfEvents(consumer)
            }
        }
    }

    private fun <T> processBatchOfEvents(consumer: KafkaConsumer<String, T>) {
        try {
            val records = consumer.poll(Duration.of(100, ChronoUnit.MILLIS))
            transformRecords<T>(records)
            consumer.commitSync()

        } catch (e: RetriableException) {
            log.warn("Failed to poll, but with a retriable exception so will continue to next loop", e)

        } catch (e: Exception) {
            log.error("Something unrecoverable happened", e)
            cancel()
        }
    }

    private fun <T> transformRecords(records: ConsumerRecords<String, T>) {
        val transformed = mutableListOf<T>()
        records.forEach { record ->
            MESSAGES_SEEN.labels(record.topic(), record.partition().toString()).inc()
            val info = record.value()
            log.info("Event funnet: $info")
            transformed.add(info)
        }
    }

}
