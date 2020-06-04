package no.nav.personbruker.dittnav.eventaggregator.metrics.db

import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.config.isOtherEnvironmentThanProd
import no.nav.personbruker.dittnav.eventaggregator.done.DoneRepository
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksRepository
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveRepository
import org.slf4j.LoggerFactory

class CacheEventCounterService(val environment: Environment,
                               val beskjedRepository: BeskjedRepository,
                               val innboksRepository: InnboksRepository,
                               val oppgaveRepository: OppgaveRepository,
                               val doneRepository: DoneRepository
) {

    private val log = LoggerFactory.getLogger(CacheEventCounterService::class.java)

    suspend fun countAllEvents(): NumberOfCachedRecords {
        return try {
            val result = NumberOfCachedRecords(
                    beskjedRepository.getNumberOfActiveEvents(),
                    beskjedRepository.getNumberOfInactiveEvents(),
                    innboksRepository.getNumberOfActiveEvents(),
                    innboksRepository.getNumberOfInactiveEvents(),
                    oppgaveRepository.getNumberOfActiveEvents(),
                    oppgaveRepository.getNumberOfInactiveEvents(),
                    doneRepository.getTotalNumberOfEvents()
            )
            log.info("Fant følgende eventer:\n$result")
            result

        } catch (e: Exception) {
            log.warn("Klarte ikke å utføre alle telleoperasjonene mot cache-en.", e)
            NumberOfCachedRecords()
        }
    }

    suspend fun countBeskjeder(): Long {
        return try {
            beskjedRepository.getTotalNumberOfEvents()

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall beskjed-eventer", e)
            -1
        }
    }

    suspend fun countInnboksEventer(): Long {
        return if (isOtherEnvironmentThanProd()) {
            try {
                innboksRepository.getTotalNumberOfEvents()

            } catch (e: Exception) {
                log.warn("Klarte ikke å telle antall innboks-eventer", e)
                -1L
            }
        } else {
            0L
        }
    }

    suspend fun countOppgaver(): Long {
        return try {
            oppgaveRepository.getTotalNumberOfEvents()

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall oppgave-eventer", e)
            -1
        }
    }

    suspend fun countDoneEvents(): Long {
        return try {
            doneRepository.getTotalNumberOfEvents()

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall done-eventer", e)
            -1
        }
    }

    suspend fun countAllDoneEvents(): Long {
        return try {
            val doneEventsInWaitingTable = doneRepository.getTotalNumberOfEvents()
            val inactiveBeskjeder = beskjedRepository.getNumberOfInactiveEvents()
            val inactiveInnbokseventer = innboksRepository.getNumberOfInactiveEvents()
            val inactiveOppgave = oppgaveRepository.getNumberOfInactiveEvents()
            val totalNumberOfDoneEvents = doneEventsInWaitingTable + inactiveBeskjeder + inactiveInnbokseventer + inactiveOppgave

            totalNumberOfDoneEvents

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall done-eventer", e)
            -1
        }
    }

}
