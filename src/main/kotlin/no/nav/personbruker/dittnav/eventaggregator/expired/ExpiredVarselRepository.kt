package no.nav.personbruker.dittnav.eventaggregator.expired

import no.nav.personbruker.dittnav.eventaggregator.beskjed.setExpiredBeskjedAsInactive
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.oppgave.setExpiredOppgaveAsInactive
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselHendelse

class ExpiredVarselRepository(private val database: Database) {

    suspend fun updateAllExpiredBeskjed(): List<VarselHendelse> {
        return database.queryWithExceptionTranslation {
            setExpiredBeskjedAsInactive()
        }
    }

    suspend fun updateAllExpiredOppgave(): List<VarselHendelse> {
        return database.queryWithExceptionTranslation {
            setExpiredOppgaveAsInactive()
        }
    }
}
