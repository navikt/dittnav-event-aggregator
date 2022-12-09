package no.nav.personbruker.dittnav.eventaggregator.expired

import no.nav.personbruker.dittnav.eventaggregator.beskjed.setExpiredBeskjedAsInactive
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.oppgave.setExpiredOppgaveAsInactive

class ExpiredVarselRepository(private val database: Database) {

    suspend fun updateAllExpiredBeskjed(): List<String> {
        return database.queryWithExceptionTranslation {
            setExpiredBeskjedAsInactive()
        }
    }

    suspend fun updateAllExpiredOppgave(): List<String> {
        return database.queryWithExceptionTranslation {
            setExpiredOppgaveAsInactive()
        }
    }
}
