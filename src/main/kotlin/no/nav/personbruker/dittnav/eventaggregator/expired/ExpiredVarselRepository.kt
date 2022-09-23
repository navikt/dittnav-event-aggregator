package no.nav.personbruker.dittnav.eventaggregator.expired

import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.setExpiredBeskjedAsInactive
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.oppgave.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.setExpiredOppgaveAsInactive

class ExpiredVarselRepository(private val database: Database) {

    suspend fun updateAllExpiredBeskjed(): Int {
        return database.queryWithExceptionTranslation {
            setExpiredBeskjedAsInactive()
        }
    }

    suspend fun updateAllExpiredOppgave(): Int {
        return database.queryWithExceptionTranslation {
            setExpiredOppgaveAsInactive()
        }
    }
}
