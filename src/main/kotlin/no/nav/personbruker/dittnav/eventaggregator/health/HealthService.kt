package no.nav.personbruker.dittnav.eventaggregator.health

import no.nav.personbruker.dittnav.eventaggregator.config.ApplicationContext

class HealthService(private val applicationContext: ApplicationContext) {

    suspend fun getHealthChecks(): List<HealthStatus> {
        return listOf(
            applicationContext.database.status(),
            applicationContext.periodicDoneEventWaitingTableProcessor.status(),
            applicationContext.periodicExpiredVarselProcessor.status(),
        )
    }
}
