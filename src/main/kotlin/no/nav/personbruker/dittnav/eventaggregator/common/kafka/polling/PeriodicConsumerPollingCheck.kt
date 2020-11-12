package no.nav.personbruker.dittnav.eventaggregator.common.kafka.polling

import kotlinx.coroutines.*
import kotlinx.coroutines.time.delay
import no.nav.personbruker.dittnav.eventaggregator.config.ApplicationContext
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.KafkaConsumerSetup
import no.nav.personbruker.dittnav.eventaggregator.health.HealthStatus
import no.nav.personbruker.dittnav.eventaggregator.health.Status
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import kotlin.coroutines.CoroutineContext

class PeriodicConsumerPollingCheck(
        private val appContext: ApplicationContext,
        private val job: Job = Job()) : CoroutineScope {

    private val log: Logger = LoggerFactory.getLogger(PeriodicConsumerPollingCheck::class.java)
    private val minutesToWait = Duration.ofMinutes(30)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    fun start() {
        log.info("Periodisk sjekking av at konsumerne kjører har blitt aktivert, første sjekk skjer om $minutesToWait minutter.")
        launch {
            while (job.isActive) {
                delay(minutesToWait)
                checkIfConsumersAreRunningAndRestartIfNot()
            }
        }
    }

    suspend fun checkIfConsumersAreRunningAndRestartIfNot() {
        val stoppedConsumers = getConsumersThatHaveStopped()
        if (stoppedConsumers.isNotEmpty()) {
            restartPolling(stoppedConsumers)
        }
    }

    fun getConsumersThatHaveStopped(): MutableList<EventType> {
        val stoppedConsumers = mutableListOf<EventType>()

        if (appContext.beskjedConsumer.isStopped()) {
            stoppedConsumers.add(EventType.BESKJED)
        }
        if (appContext.doneConsumer.isStopped()) {
            stoppedConsumers.add(EventType.DONE)
        }
        if (appContext.oppgaveConsumer.isStopped()) {
            stoppedConsumers.add(EventType.OPPGAVE)
        }
        return stoppedConsumers
    }

    suspend fun restartPolling(stoppedConsumers: MutableList<EventType>) {
        log.warn("Følgende konsumere hadde stoppet ${stoppedConsumers}, de(n) vil bli restartet.")
        KafkaConsumerSetup.restartPolling(appContext)
        log.info("$stoppedConsumers konsumern(e) har blitt restartet.")
    }

    suspend fun stop() {
        log.info("Stopper periodisk sjekking av at konsumerne kjører.")
        job.cancelAndJoin()
    }

    fun isCompleted(): Boolean {
        return job.isCompleted
    }

    fun status(): HealthStatus {
        return when (job.isActive) {
            true -> HealthStatus("PeriodicConsumerPollingCheck", Status.OK, "Checker is running", false)
            false -> HealthStatus("PeriodicConsumerPollingCheck", Status.ERROR, "Checker is not running", false)
        }
    }

}