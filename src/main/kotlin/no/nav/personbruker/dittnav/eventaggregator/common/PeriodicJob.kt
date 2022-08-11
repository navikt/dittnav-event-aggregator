package no.nav.personbruker.dittnav.eventaggregator.common

import kotlinx.coroutines.*
import kotlinx.coroutines.CoroutineStart.LAZY
import no.nav.personbruker.dittnav.eventaggregator.health.HealthStatus
import no.nav.personbruker.dittnav.eventaggregator.health.Status
import java.time.Duration

abstract class PeriodicJob(private val interval: Duration) {

    abstract val job: Job

    fun initializeJob(periodicProcess: suspend () -> Unit) = runBlocking {
        launch(start = LAZY) {
            while (isActive) {
                periodicProcess()
                delay(interval.toMillis())
            }
        }
    }

    fun status(): HealthStatus {
        return when (job.isActive) {
            true -> HealthStatus(className(), Status.OK, "${className()} is running", false)
            false -> HealthStatus(className(), Status.ERROR, "${className()} is not running", false)
        }
    }

    private fun className() = this::class.simpleName!!

    fun start() {
        job.start()
    }

    fun isCompleted(): Boolean {
        return job.isCompleted
    }

    suspend fun stop() {
        job.cancelAndJoin()
    }
}
