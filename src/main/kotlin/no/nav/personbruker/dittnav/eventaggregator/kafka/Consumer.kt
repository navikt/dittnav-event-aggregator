package no.nav.personbruker.dittnav.eventaggregator.kafka

import io.prometheus.client.Counter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import no.nav.personbruker.dittnav.eventaggregator.service.EventBatchProcessorService
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.RetriableException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.coroutines.CoroutineContext

class Consumer<T>(
        val topic: String,
        val kafkaConsumer: KafkaConsumer<String, T>,
        val eventBatchProcessorService: EventBatchProcessorService<T>,
        val job: Job = Job(),
        val MESSAGES_SEEN: Counter = initPrometheusMessageCounter(topic)
) : CoroutineScope {

    private val log: Logger = LoggerFactory.getLogger(Consumer::class.java)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    fun isRunning(): Boolean {
        return job.isActive
    }

    fun cancel() {
        job.cancel()
    }

    fun poll() {
        launch {
            kafkaConsumer.use { consumer ->
                consumer.subscribe(listOf(topic))

                while (job.isActive) {
                    processBatchOfEvents()
                }
            }
        }
    }

    private fun processBatchOfEvents() {
        try {
            val records = kafkaConsumer.poll(Duration.of(100, ChronoUnit.MILLIS))
            processRecords(records)
            kafkaConsumer.commitSync()

        } catch (e: RetriableException) {
            log.warn("Failed to poll, but with a retriable exception so will continue to next loop", e)

        } catch (e: Exception) {
            log.error("Something unrecoverable happened", e)
            cancel()
        }
    }

    private fun <T> processRecords(records: ConsumerRecords<String, T>) {
        records.forEach { record ->
            MESSAGES_SEEN.labels(record.topic(), record.partition().toString()).inc()
            log.info("Event funnet på topic-en: $topic.")

            eventBatchProcessorService.processEvent(record)
        }
    }

}

private fun initPrometheusMessageCounter(topic: String): Counter {
    val topicNameWithoutDashes = topic.replace("-", "_")
    return Counter.build()
            .name("${topicNameWithoutDashes}_messages_seen")
            .namespace("dittnav_consumer")
            .help("Messages read since last startup")
            .labelNames("topic", "partition")
            .register()
}
