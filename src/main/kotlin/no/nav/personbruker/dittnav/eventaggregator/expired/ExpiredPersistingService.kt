package no.nav.personbruker.dittnav.eventaggregator.expired

import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.getExpiredBeskjedFromCursor
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database

class ExpiredPersistingService(private val database: Database) {
    suspend fun getExpiredBeskjeder(): List<Beskjed> {
        return database.queryWithExceptionTranslation {
            getExpiredBeskjedFromCursor()
        }
    }

}
