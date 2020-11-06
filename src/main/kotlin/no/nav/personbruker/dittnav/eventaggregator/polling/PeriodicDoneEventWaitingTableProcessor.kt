package no.nav.personbruker.dittnav.eventaggregator.polling

import kotlinx.coroutines.*
import kotlinx.coroutines.time.delay
import no.nav.personbruker.dittnav.eventaggregator.config.ApplicationContext
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.health.HealthStatus
import no.nav.personbruker.dittnav.eventaggregator.health.Status
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import kotlin.coroutines.CoroutineContext

class PeriodicConsumerChecker(
    private val appContext: ApplicationContext,
    private val job: Job = Job()
) : CoroutineScope {

    private val log: Logger = LoggerFactory.getLogger(PeriodicConsumerChecker::class.java)
    private val minutesToWait = Duration.ofMinutes(30)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    fun status(): HealthStatus {
        return when (job.isActive) {
            true -> HealthStatus("PeriodicConsumerChecker", Status.OK, "Checker is running", false)
            false -> HealthStatus("PeriodicConsumerChecker", Status.ERROR, "Checker is not running", false)
        }
    }

    suspend fun stop() {
        log.info("Stopper periodisk sjekking av at konsumerne kjører")
        job.cancelAndJoin()
    }

    fun isCompleted(): Boolean {
        return job.isCompleted
    }

    fun start() {
        log.info("Periodisk sjekking av at konsumerne kjører har blitt aktivert, første sjekk skjer om $minutesToWait minutter.")
        launch {
            while (job.isActive) {
                delay(minutesToWait)
                checkIfConsumersAreRunningAndRestartIfNot()
            }
        }
    }

    private suspend fun checkIfConsumersAreRunningAndRestartIfNot() {
        val stoppedConsumers = mutableListOf<EventType>()
        if(appContext.beskjedConsumer.isStopped()) {
            stoppedConsumers.add(EventType.BESKJED)
        }
        if(appContext.doneConsumer.isStopped()) {
            stoppedConsumers.add(EventType.DONE)
        }
        if(appContext.oppgaveConsumer.isStopped()) {
            stoppedConsumers.add(EventType.OPPGAVE)
        }

        if (stoppedConsumers.isNotEmpty()) {
            log.warn("Følgende konsumere hadde stoppet $stoppedConsumers, de(n) vil bli restartet.")
            appContext.restartPolling()
            log.info("Konsumerne har blitt restartet.")
        }
    }

}
