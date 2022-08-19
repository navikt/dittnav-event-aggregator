package no.nav.personbruker.dittnav.eventaggregator.common.kafka.polling

import no.nav.personbruker.dittnav.eventaggregator.common.PeriodicJob
import no.nav.personbruker.dittnav.eventaggregator.config.ApplicationContext
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.KafkaConsumerSetup
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

class PeriodicConsumerPollingCheck(private val appContext: ApplicationContext)
    : PeriodicJob(interval = Duration.ofMinutes(30)) {

    private val log: Logger = LoggerFactory.getLogger(PeriodicConsumerPollingCheck::class.java)

    override val job = initializeJob {
        checkIfConsumersAreRunningAndRestartIfNot()
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
            stoppedConsumers.add(EventType.BESKJED_INTERN)
        }
        if (appContext.doneConsumer.isStopped()) {
            stoppedConsumers.add(EventType.DONE_INTERN)
        }
        if (appContext.oppgaveConsumer.isStopped()) {
            stoppedConsumers.add(EventType.OPPGAVE_INTERN)
        }
        return stoppedConsumers
    }

    private suspend fun restartPolling(stoppedConsumers: MutableList<EventType>) {
        log.warn("Følgende konsumere hadde stoppet ${stoppedConsumers}, de(n) vil bli restartet.")
        KafkaConsumerSetup.restartPolling(appContext)
        log.info("$stoppedConsumers konsumern(e) har blitt restartet.")
    }
}
