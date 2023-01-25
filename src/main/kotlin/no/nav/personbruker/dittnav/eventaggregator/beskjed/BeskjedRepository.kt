package no.nav.personbruker.dittnav.eventaggregator.beskjed

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselHendelse
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import no.nav.personbruker.dittnav.eventaggregator.varsel.setVarselInaktiv
import java.sql.Connection

class BeskjedRepository(val database: Database) {

    fun setBeskjedInactive(eventId: String, fnr: String): VarselHendelse? =
        runBlocking {
            database.dbQuery {
                requireBeskjedExists(eventId, fnr)
                setVarselInaktiv(eventId, VarselType.BESKJED)
            }
        }
}

private fun Connection.requireBeskjedExists(eventId: String, fnr: String) {
    prepareStatement("""SELECT * FROM beskjed WHERE eventId=?""".trimMargin())
        .use {
            it.setString(1, eventId)
            it.executeQuery().apply {
                if (!next()) {
                    throw BeskjedNotFoundException(eventId)
                }
                if (getString("fodselsnummer") != fnr) {
                    throw BeskjedDoesNotBelongToUserException(eventId)
                }
            }
        }
}
