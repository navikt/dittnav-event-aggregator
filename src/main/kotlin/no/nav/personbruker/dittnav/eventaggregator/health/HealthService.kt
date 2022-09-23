package no.nav.personbruker.dittnav.eventaggregator.health

import no.nav.personbruker.dittnav.eventaggregator.config.ApplicationContext

class HealthService(private val applicationContext: ApplicationContext) {

    suspend fun getHealthChecks(): List<HealthStatus> {
        return listOf(
            applicationContext.database.status(),
            applicationContext.beskjedConsumer.status(),
            applicationContext.innboksConsumer.status(),
            applicationContext.oppgaveConsumer.status(),
            applicationContext.doneConsumer.status(),
            applicationContext.periodicDoneEventWaitingTableProcessor.status(),
            applicationContext.periodicConsumerPollingCheck.status(),
            applicationContext.periodicExpiredVarselProcessor.status(),
            applicationContext.doknotifikasjonStatusConsumer.status()
        )
    }
}
