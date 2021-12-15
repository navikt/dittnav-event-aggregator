package no.nav.personbruker.dittnav.eventaggregator.expired

import kotlinx.coroutines.*
import kotlinx.coroutines.time.delay
import no.nav.personbruker.dittnav.eventaggregator.health.HealthStatus
import no.nav.personbruker.dittnav.eventaggregator.health.Status
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import kotlin.coroutines.CoroutineContext

class PeriodicExpiredBeskjedProcessor(
    private val expiredPersistingService: ExpiredPersistingService,
    private val doneEventEmitter: DoneEventEmitter,
    private val job: Job = Job()
) : CoroutineScope {

    private val log: Logger = LoggerFactory.getLogger(PeriodicExpiredBeskjedProcessor::class.java)
    private val timeToWait = Duration.ofMinutes(10)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    fun status(): HealthStatus {
        return when (job.isActive) {
            true -> HealthStatus("ExpiredBeskjedProcessor", Status.OK, "Processor is running", false)
            false -> HealthStatus("ExpiredBeskjedProcessor", Status.ERROR, "Processor is not running", false)
        }
    }

    suspend fun stop() {
        log.info("Stopper periodisk prosessering av utgått beskjeder")
        job.cancelAndJoin()
    }

    fun isCompleted(): Boolean {
        return job.isCompleted
    }

    fun start() {
        log.info("Periodisk prosessering av utgått beskjeder har blitt aktivert, første prosessering skjer om $timeToWait minutter.")
        launch {
            while (job.isActive) {
                delay(timeToWait)
                sendDoneEventsForExpiredBeskjeder()
            }
        }
    }

    suspend fun sendDoneEventsForExpiredBeskjeder() {
        try {
            val beskjeder = expiredPersistingService.getExpiredBeskjeder()
            if (beskjeder.isEmpty()) {
                log.info("Ingen utgått beskjed å prosessere")
                return
            }

            doneEventEmitter.emittBeskjedDone(beskjeder)
            log.info("Har prosessert {} utgått beskjeder", beskjeder.size)
        } catch (e: Exception) {
            log.error("Uventet feil ved processering av utgått beskjeder", e)
        }
    }
}
