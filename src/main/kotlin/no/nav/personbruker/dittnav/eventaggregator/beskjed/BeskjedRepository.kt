package no.nav.personbruker.dittnav.eventaggregator.beskjed

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database

class BeskjedRepository(val database: Database) {

    fun setBeskjedInactive(eventId: String, fnr: String): Int =
        runBlocking {
            database.dbQuery {
                this.setBeskjedInaktiv(eventId, fnr)
            }
        }
}