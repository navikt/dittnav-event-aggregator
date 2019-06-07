package no.nav.personbruker

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
        this.job = Job()
        this.kafkaProps = kafkaProps
        this.topics = topics
    }


    fun <T> fetchFromKafka() {
        KafkaConsumer<String, T>(kafkaProps).use { consumer ->
            consumer.subscribe(topics)
            var counter = 0
            while (job.isActive) {
                try {
                    val records = consumer.poll(Duration.of(100, ChronoUnit.MILLIS))

                    if (counter++ % 50 == 0 && records.isEmpty) {
                        log.info("Ingen nye eventer ble funnet på topic-en: " + topics.get(0))
                    }

                    transformRecords<T>(records)

//                    consumer.commitSync()

                } catch (e: RetriableException) {
                    log.warn("Failed to poll, but with a retriable exception so will continue to next loop", e)

                } catch (e: Exception) {
                    log.error("Something unrecoverable happened", e)
                    cancel()
                }
            }
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
