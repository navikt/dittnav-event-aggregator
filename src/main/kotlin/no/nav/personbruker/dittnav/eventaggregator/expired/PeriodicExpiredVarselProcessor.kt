package no.nav.personbruker.dittnav.eventaggregator.expired

import mu.KotlinLogging
import no.nav.personbruker.dittnav.eventaggregator.common.PeriodicJob
import no.nav.personbruker.dittnav.eventaggregator.done.VarselInaktivertKilde.Frist
import no.nav.personbruker.dittnav.eventaggregator.done.VarselInaktivertProducer
import java.time.Duration

class PeriodicExpiredVarselProcessor(
    private val expiredVarselRepository: ExpiredVarselRepository,
    private val varselInaktivertProducer: VarselInaktivertProducer,
    private val expiredMetricProbe: ExpiredMetricsProbe,
    interval: Duration = Duration.ofMinutes(10)
) : PeriodicJob(interval) {

    private val log = KotlinLogging.logger { }

    override val job = initializeJob {
        updateExpiredBeskjed()
        updateExpiredOppgave()
    }

    suspend fun updateExpiredOppgave() {
        try {
            val varselHendelser = expiredVarselRepository.updateAllExpiredOppgave()

            if (varselHendelser.isNotEmpty()) {
                varselHendelser.forEach { varselInaktivertProducer.varselInaktivert(it, Frist) }
                log.info("Prosesserte ${varselHendelser.size} utgåtte oppgaver.")
                expiredMetricProbe.countOppgaveExpired(varselHendelser)
            } else {
                log.info("Ingen oppgaver har utgått siden forrige sjekk.")
            }
        } catch (e: Exception) {
            log.error("Uventet feil ved prosessering av utgåtte oppgaver", e)
        }
    }

    suspend fun updateExpiredBeskjed() {
        try {
            val varselHendelser = expiredVarselRepository.updateAllExpiredBeskjed()

            if (varselHendelser.isNotEmpty()) {
                varselHendelser.forEach { varselInaktivertProducer.varselInaktivert(it, Frist) }
                log.info("Prosesserte ${varselHendelser.size} utgåtte beskjeder.")
                expiredMetricProbe.countBeskjedExpired(varselHendelser)
            } else {
                log.info("Ingen beskjeder har utgått siden forrige sjekk.")
            }
        } catch (e: Exception) {
            log.error("Uventet feil ved prosessering av utgåtte beskjeder", e)
        }
    }
}
