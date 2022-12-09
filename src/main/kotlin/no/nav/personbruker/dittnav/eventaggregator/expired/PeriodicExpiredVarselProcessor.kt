package no.nav.personbruker.dittnav.eventaggregator.expired

import no.nav.personbruker.dittnav.eventaggregator.common.PeriodicJob
import no.nav.personbruker.dittnav.eventaggregator.done.VarselInaktivertProducer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

class PeriodicExpiredVarselProcessor(
    private val expiredVarselRepository: ExpiredVarselRepository,
    private val varselInaktivertProducer: VarselInaktivertProducer,
    interval: Duration = Duration.ofMinutes(10)
) : PeriodicJob(interval) {

    private val log: Logger = LoggerFactory.getLogger(PeriodicExpiredVarselProcessor::class.java)

    override val job = initializeJob {
        updateExpiredBeskjed()
        updateExpiredOppgave()
    }

    suspend fun updateExpiredOppgave() {
        try {
            val expiredEventIds =  expiredVarselRepository.updateAllExpiredOppgave()

            if (expiredEventIds.size > 0) {
                expiredEventIds.forEach { varselInaktivertProducer.cancelEksternVarsling(it) }
                log.info("Prosesserte ${expiredEventIds.size} utgåtte oppgaver.")
            } else {
                log.info("Ingen oppgaver har utgått siden forrige sjekk.")
            }
        } catch (e: Exception) {
            log.error("Uventet feil ved prosessering av utgåtte oppgaver", e)
        }
    }

    suspend fun updateExpiredBeskjed() {
        try {
            val expiredEventIds = expiredVarselRepository.updateAllExpiredBeskjed()

            if (expiredEventIds.size > 0) {
                expiredEventIds.forEach { varselInaktivertProducer.cancelEksternVarsling(it) }
                log.info("Prosesserte ${expiredEventIds.size} utgåtte beskjeder.")
            } else {
                log.info("Ingen beskjeder har utgått siden forrige sjekk.")
            }
        } catch (e: Exception) {
            log.error("Uventet feil ved prosessering av utgåtte beskjeder", e)
        }
    }
}
