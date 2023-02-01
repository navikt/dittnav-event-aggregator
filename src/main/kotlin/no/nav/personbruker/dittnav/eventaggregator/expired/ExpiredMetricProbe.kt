package no.nav.personbruker.dittnav.eventaggregator.expired

import no.nav.personbruker.dittnav.common.metrics.MetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.metrics.DB_EVENTS_EXPIRED
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselHendelse
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType

class ExpiredMetricsProbe(private val metricsReporter: MetricsReporter) {

    suspend fun countBeskjedExpired(events: List<VarselHendelse>) {
        events.groupBy { it.appnavn }
            .forEach { (produsentApp, archived) ->
                reportExpired(archived.size, VarselType.BESKJED, produsentApp)
            }
    }

    suspend fun countOppgaveExpired(events: List<VarselHendelse>) {
        events.groupBy { it.appnavn }
            .forEach { (produsentApp, archived) ->
                reportExpired(archived.size, VarselType.OPPGAVE, produsentApp)
            }
    }

    private suspend fun reportExpired(count: Int, eventType: VarselType, producerApp: String) {
        metricsReporter.registerDataPoint(
            DB_EVENTS_EXPIRED,
            counterField(count),
            mapOf("eventType" to eventType.name, "producer" to producerApp)
        )
    }

    private fun counterField(varsel: Int): Map<String, Int> = mapOf("counter" to varsel)

}
