package no.nav.personbruker.dittnav.eventaggregator.metrics.db.count

import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.config.isOtherEnvironmentThanProd
import org.slf4j.LoggerFactory

class CacheEventCounterService(val environment: Environment, val repository: MetricsRepository) {

    private val log = LoggerFactory.getLogger(CacheEventCounterService::class.java)

    suspend fun countAllEvents(): NumberOfCachedRecords {
        return try {
            val result = NumberOfCachedRecords(
                    repository.getNumberOfActiveBeskjedEvents(),
                    repository.getNumberOfInactiveBeskjedEvents(),
                    repository.getNumberOfActiveInnboksEvents(),
                    repository.getNumberOfInactiveInnboksEvents(),
                    repository.getNumberOfActiveOppgaveEvents(),
                    repository.getNumberOfInactiveOppgaveEvents(),
                    repository.getTotalNumberOfDoneEvents()
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
            repository.getTotalNumberOfBeskjedEvents()

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall beskjed-eventer", e)
            -1
        }
    }

    suspend fun countInnboksEventer(): Long {
        return if (isOtherEnvironmentThanProd()) {
            try {
                repository.getTotalNumberOfInnboksEvents()

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
            repository.getTotalNumberOfOppgaveEvents()

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall oppgave-eventer", e)
            -1
        }
    }

    suspend fun countDoneEvents(): Long {
        return try {
            repository.getTotalNumberOfDoneEvents()

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall done-eventer", e)
            -1
        }
    }

    suspend fun countAllDoneEvents(): Long {
        return try {
            val doneEventsInWaitingTable = repository.getTotalNumberOfDoneEvents()
            val inactiveBeskjeder = repository.getNumberOfInactiveBeskjedEvents()
            val inactiveInnbokseventer = repository.getNumberOfInactiveInnboksEvents()
            val inactiveOppgave = repository.getNumberOfInactiveOppgaveEvents()
            val totalNumberOfDoneEvents = doneEventsInWaitingTable + inactiveBeskjeder + inactiveInnbokseventer + inactiveOppgave

            totalNumberOfDoneEvents

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle antall done-eventer", e)
            -1
        }
    }

}
