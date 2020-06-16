package no.nav.personbruker.dittnav.eventaggregator.metrics.db.count

import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MetricsRepository(private val database: Database) {

    private val log: Logger = LoggerFactory.getLogger(MetricsRepository::class.java)

    suspend fun getNumberOfActiveBeskjedEvents(): Long {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEventsByActiveStatus(EventType.BESKJED, true)
        }
    }

    suspend fun getNumberOfInactiveBeskjedEvents(): Long {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEventsByActiveStatus(EventType.BESKJED, false)
        }
    }

    suspend fun getTotalNumberOfBeskjedEvents(): Long {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEvents(EventType.BESKJED)
        }
    }

    suspend fun getNumberOfBeskjedEventsGroupedByProdusent(): Map<String, Int> {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEventsGroupedBySystembruker(EventType.BESKJED)
        }
    }

    suspend fun getNumberOfActiveInnboksEvents(): Long {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEventsByActiveStatus(EventType.INNBOKS, true)
        }
    }

    suspend fun getNumberOfInactiveInnboksEvents(): Long {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEventsByActiveStatus(EventType.INNBOKS, false)
        }
    }

    suspend fun getTotalNumberOfInnboksEvents(): Long {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEvents(EventType.INNBOKS)
        }
    }

    suspend fun getNumberOfInnboksEventsGroupedByProdusent(): Map<String, Int> {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEventsGroupedBySystembruker(EventType.INNBOKS)
        }
    }

    suspend fun getNumberOfActiveOppgaveEvents(): Long {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEventsByActiveStatus(EventType.OPPGAVE, true)
        }
    }

    suspend fun getNumberOfInactiveOppgaveEvents(): Long {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEventsByActiveStatus(EventType.OPPGAVE, false)
        }
    }

    suspend fun getTotalNumberOfOppgaveEvents(): Long {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEvents(EventType.OPPGAVE)
        }
    }

    suspend fun getTotalNumberOfDoneEvents(): Long {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEvents(EventType.DONE)
        }
    }

    suspend fun getNumberOfOppgaveEventsGroupedByProdusent(): Map<String, Int> {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEventsGroupedBySystembruker(EventType.OPPGAVE)
        }
    }

    suspend fun getNumberOfDoneEventsInWaitingTableGroupedByProdusent(): Map<String, Int> {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEventsGroupedBySystembruker(EventType.DONE)
        }
    }

    suspend fun getNumberOfInactiveBrukernotifikasjonerGroupedByProdusent(): Map<String, Int> {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfBrukernotifikasjonerByActiveStatus(false)
        }
    }

}
