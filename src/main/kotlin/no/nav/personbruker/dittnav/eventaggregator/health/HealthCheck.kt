package no.nav.personbruker.dittnav.eventaggregator.health

interface HealthCheck {

    suspend fun status(): HealthStatus

}
