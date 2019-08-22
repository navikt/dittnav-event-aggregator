package no.nav.personbruker.dittnav.eventaggregator.kafka

import io.prometheus.client.Counter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import no.nav.personbruker.dittnav.eventaggregator.database.Database
import no.nav.brukernotifikasjon.schemas.Informasjon
import no.nav.personbruker.dittnav.eventaggregator.service.InformasjonEventService
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
    lateinit var informasjonEventService: InformasjonEventService

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

    fun create(topics: List<String>, kafkaProps: Properties, database: Database) {
        job = Job()
        Consumer.kafkaProps = kafkaProps
        Consumer.topics = topics
        this.informasjonEventService = InformasjonEventService(database)
    }


    fun pollContinuouslyForEvents() {
        KafkaConsumer<String, Informasjon>(kafkaProps).use { consumer ->
            consumer.subscribe(topics)

            while (job.isActive) {
                processBatchOfEvents(consumer)
            }
        }
    }

    private fun processBatchOfEvents(consumer: KafkaConsumer<String, Informasjon>) {
        try {
            val records = consumer.poll(Duration.of(100, ChronoUnit.MILLIS))
            transformRecords(records)
            consumer.commitSync()

        } catch (e: RetriableException) {
            log.warn("Failed to poll, but with a retriable exception so will continue to next loop", e)

        } catch (e: Exception) {
            log.error("Something unrecoverable happened", e)
            cancel()
        }
    }

    private fun transformRecords(records: ConsumerRecords<String, Informasjon>) {
        records.forEach { record ->
            MESSAGES_SEEN.labels(record.topic(), record.partition().toString()).inc()
            val info = record.value()
            log.info("Event funnet: $info")

            informasjonEventService.storeEventInCache(info)
        }
    }

}
