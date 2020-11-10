package no.nav.personbruker.dittnav.eventaggregator.common.kafka

import kotlinx.coroutines.*
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.RetriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UnretriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.health.HealthCheck
import no.nav.personbruker.dittnav.eventaggregator.health.HealthStatus
import no.nav.personbruker.dittnav.eventaggregator.health.Status
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.RetriableException
import org.apache.kafka.common.errors.TopicAuthorizationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.coroutines.CoroutineContext

class Consumer<T>(
        val topic: String,
        val kafkaConsumer: KafkaConsumer<Nokkel, T>,
        val eventBatchProcessorService: EventBatchProcessorService<T>,
        val job: Job = Job(),
        val maxPollTimeout: Long = 100L
) : CoroutineScope, HealthCheck {

    private val log: Logger = LoggerFactory.getLogger(Consumer::class.java)

    companion object {
        private const val ONE_MINUTE_IN_MS = 60000L
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    suspend fun stopPolling() {
        job.cancelAndJoin()
    }

    fun isCompleted(): Boolean {
        return job.isCompleted
    }

    override suspend fun status(): HealthStatus {
        val serviceName = topic + "consumer"
        return if (job.isActive) {
            HealthStatus(serviceName, Status.OK, "Consumer is running", includeInReadiness = false)
        } else {
            log.error("Selftest mot Kafka-consumere feilet, consumer kjører ikke.")
            HealthStatus(serviceName, Status.ERROR, "Consumer is not running", includeInReadiness = false)
        }
    }

    fun isStopped(): Boolean {
        return !job.isActive
    }

    fun startPolling() {
        launch {
            log.info("Starter en coroutine for polling på topic-en $topic.")
            kafkaConsumer.use { consumer ->
                consumer.subscribe(listOf(topic))

                while (job.isActive) {
                    processBatchOfEvents()
                }
            }
        }
    }

    private suspend fun processBatchOfEvents() = withContext(Dispatchers.IO) {
        try {
            val records = kafkaConsumer.poll(Duration.of(maxPollTimeout, ChronoUnit.MILLIS))
            if (records.containsEvents()) {
                eventBatchProcessorService.processEvents(records)
                kafkaConsumer.commitSync()
            }
        } catch (rde: RetriableDatabaseException) {
            log.warn("Klarte ikke å skrive til databasen, prøver igjen senrere. Topic: $topic", rde)
            rollbackOffset()

        } catch (re: RetriableException) {
            log.warn("Polling mot Kafka feilet, prøver igjen senere. Topic: $topic", re)
            rollbackOffset()

        } catch (ure: UntransformableRecordException) {
            val msg = "Et eller flere eventer kunne ikke transformeres, stopper videre polling. Topic: $topic. \n Bruker appen sisteversjon av brukernotifikasjon-schemas?"
            log.error(msg, ure)
            stopPolling()

        } catch (ude: UnretriableDatabaseException) {
            log.error("Det skjedde en alvorlig feil mot databasen, stopper videre polling. Topic: $topic", ude)
            stopPolling()

        } catch (ce: CancellationException) {
            log.info("Denne coroutine-en ble stoppet. ${ce.message}", ce)

        } catch (tae: TopicAuthorizationException) {
            log.warn("Pauser polling i ett minutt, er ikke autorisert for å lese: ${tae.unauthorizedTopics()}", tae)
            delay(ONE_MINUTE_IN_MS)

        } catch (e: Exception) {
            log.error("Noe uventet feilet, stopper polling. Topic: $topic", e)
            stopPolling()
        }
    }

    fun ConsumerRecords<Nokkel, T>.containsEvents() = count() > 0

    private suspend fun rollbackOffset() {
        withContext(Dispatchers.IO) {
            kafkaConsumer.rollbackToLastCommitted()
        }
    }
}
