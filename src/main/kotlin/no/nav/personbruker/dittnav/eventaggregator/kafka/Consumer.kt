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
        val MESSAGES_SEEN: Counter = initPrometheousMessageCounter(topic)
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

    private var counter = 0
    fun poll() {
        launch {
            kafkaConsumer.use { consumer ->
                consumer.subscribe(listOf(topic))

                while (job.isActive) {
                    processBatchOfEvents(consumer)
                    progressOutput()
                }
            }
        }
    }

    private fun progressOutput() {
        counter++
        if (counter % 1000 == 0) {
            log.info("Poller på topic-en: $topic")
        }
    }

    private fun <T> processBatchOfEvents(consumer: KafkaConsumer<String, T>) {
        try {
            val records = consumer.poll(Duration.of(100, ChronoUnit.MILLIS))
            processRecords(records)
            consumer.commitSync()

        } catch (e: RetriableException) {
            log.warn("Failed to poll, but with a retryable exception so will continue to next loop", e)

        } catch (e: Exception) {
            log.error("Something unrecoverable happened", e)
            cancel()
        }
    }

    private fun <T> processRecords(records: ConsumerRecords<String, T>) {
        records.forEach { record ->
            MESSAGES_SEEN.labels(record.topic(), record.partition().toString()).inc()
            log.info("Event funnet på $topic:")

            eventBatchProcessorService.processEvent(record)
        }
    }

}

private fun initPrometheousMessageCounter(topic: String) : Counter {
    var topicNameWithoutDashes = topic.replace("-", "_")
    return Counter.build()
            .name("${topicNameWithoutDashes}_messages_seen")
            .namespace("dittnav_consumer")
            .help("Messages read since last startup")
            .labelNames("topic", "partition")
            .register()
}
