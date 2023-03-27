package no.nav.personbruker.dittnav.eventaggregator.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.LAZY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration

abstract class PeriodicJob(private val interval: Duration) {

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    abstract val job: Job

    protected fun initializeJob(periodicProcess: suspend () -> Unit) = scope.launch(start = LAZY) {
        while (job.isActive) {
            periodicProcess()
            delay(interval.toMillis())
        }
    }

    fun start() {
        job.start()
    }

    suspend fun stop() {
        job.cancelAndJoin()
    }
}
