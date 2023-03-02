package no.nav.personbruker.dittnav.eventaggregator.expired

import no.nav.personbruker.dittnav.eventaggregator.common.PeriodicJob
import no.nav.personbruker.dittnav.eventaggregator.done.VarselInaktivertKilde
import no.nav.personbruker.dittnav.eventaggregator.done.VarselInaktivertKilde.Frist
import no.nav.personbruker.dittnav.eventaggregator.done.VarselInaktivertProducer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

class PeriodicExpiredVarselProcessor(
    private val expiredVarselRepository: ExpiredVarselRepository,
    private val varselInaktivertProducer: VarselInaktivertProducer,
    private val expiredMetricProbe: ExpiredMetricsProbe,
    interval: Duration = Duration.ofMinutes(10)
) : PeriodicJob(interval) {

    private val log: Logger = LoggerFactory.getLogger(PeriodicExpiredVarselProcessor::class.java)

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
