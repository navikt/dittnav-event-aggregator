package no.nav.personbruker.dittnav.eventaggregator.expired

import no.nav.personbruker.dittnav.eventaggregator.common.PeriodicJob
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

class PeriodicExpiredVarselProcessor(
    private val expiredVarselRepository: ExpiredVarselRepository,
    interval: Duration = Duration.ofMinutes(10)
) : PeriodicJob(interval) {

    private val log: Logger = LoggerFactory.getLogger(PeriodicExpiredVarselProcessor::class.java)

    override val job = initializeJob {
        updateExpiredBeskjed()
        updateExpiredOppgave()
    }

    suspend fun updateExpiredOppgave() {
        try {
            val numberExpired =  expiredVarselRepository.updateAllExpiredOppgave()

            if (numberExpired == 0) {
                log.info("Ingen oppgaver har utgått siden forrige sjekk.")
            } else {
                log.info("Prosesserte $numberExpired utgåtte oppgaver.")
            }
        } catch (e: Exception) {
            log.error("Uventet feil ved prosessering av utgåtte oppgaver", e)
        }
    }

    suspend fun updateExpiredBeskjed() {
        try {
            val numberExpired = expiredVarselRepository.updateAllExpiredBeskjed()

            if (numberExpired == 0) {
                log.info("Ingen beskjeder har utgått siden forrige sjekk.")
            } else {
                log.info("Prosesserte $numberExpired utgåtte beskjeder.")
            }
        } catch (e: Exception) {
            log.error("Uventet feil ved prosessering av utgåtte beskjeder", e)
        }
    }
}
