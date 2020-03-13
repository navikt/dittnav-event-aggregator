package no.nav.personbruker.dittnav.eventaggregator.common.kafka

import kotlinx.coroutines.*
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.RetriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UnretriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
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
        val kafkaConsumer: KafkaConsumer<Nokkel, T>,
        val eventBatchProcessorService: EventBatchProcessorService<T>,
        val job: Job = Job()
) : CoroutineScope {

    private val log: Logger = LoggerFactory.getLogger(Consumer::class.java)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    fun isRunning(): Boolean {
        return job.isActive
    }

    fun stopPolling() {
        job.cancel()
    }

    fun startPolling() {
        launch {
            kafkaConsumer.use { consumer ->
                consumer.subscribe(listOf(topic))

                while (job.isActive) {
                    processBatchOfEvents()
                }
            }
        }
    }

    private suspend fun processBatchOfEvents() {
        try {
            withContext(Dispatchers.IO) {
                kafkaConsumer.poll(Duration.of(100, ChronoUnit.MILLIS))
            }.takeIf {
                records -> records.count() > 0
            }?.let { records ->
                eventBatchProcessorService.processEvents(records)
                logDebugOutput(records)
                commitSync()
            }
        } catch (rde: RetriableDatabaseException) {
            log.warn("Klarte ikke å skrive til databasen, prøver igjen senrere. Topic: $topic", rde)

        } catch (re: RetriableException) {
            log.warn("Polling mot Kafka feilet, prøver igjen senere. Topic: $topic", re)

        } catch (ure: UntransformableRecordException) {
            val msg = "Et eller flere eventer kunne ikke transformeres, stopper videre polling. Topic: $topic. \n Bruker appen sisteversjon av brukernotifikasjon-schemas?"
            log.error(msg, ure)
            stopPolling()

        } catch (ude: UnretriableDatabaseException) {
            log.error("Det skjedde en alvorlig feil mot databasen, stopper videre polling. Topic: $topic", ude)
            stopPolling()

        } catch (e: Exception) {
            log.error("Noe uventet feilet, stopper polling. Topic: $topic", e)
            stopPolling()
        }
    }

    private fun logDebugOutput(records: ConsumerRecords<Nokkel, T>) {
        log.info("Fant ${records.count()} eventer funnet på topic-en: $topic.")
    }

    private suspend fun commitSync() {
        withContext(Dispatchers.IO) {
            kafkaConsumer.commitSync()
        }
    }
}