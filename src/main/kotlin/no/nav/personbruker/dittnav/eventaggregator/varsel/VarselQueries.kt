package no.nav.personbruker.dittnav.eventaggregator.varsel

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper
import no.nav.personbruker.dittnav.eventaggregator.common.database.*
import no.nav.personbruker.dittnav.eventaggregator.done.Done
import no.nav.personbruker.dittnav.eventaggregator.varsel.HendelseType.Inaktivert
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types


fun Connection.getVarsel(eventId: String): VarselHeader? =
    prepareStatement("""SELECT * FROM varsel_header_view WHERE eventid = ?""")
        .use {
            it.setString(1, eventId)
            it.executeQuery().singleResultOrNull {
                toVarsel()
            }
        }

fun Connection.setVarselInaktiv(eventId: String, varselType: VarselType): VarselHendelse? =
    prepareStatement(
        """
            UPDATE ${VarselTable.fromVarselType(varselType)} SET aktiv = FALSE, frist_utløpt= FALSE, sistoppdatert = ? 
              WHERE eventId = ? AND aktiv=TRUE
            RETURNING appnavn
        """
            .trimMargin())
        .use {
            it.setObject(1, LocalDateTimeHelper.nowAtUtc(), Types.TIMESTAMP)
            it.setString(2, eventId)
            it.executeQuery().singleResultOrNull {
                VarselHendelse(Inaktivert, varselType, appnavn = getString("appnavn"), eventId = eventId)
            }
        }

fun Connection.setVarslerInaktiv(doneEvents: List<Done>, varselType: VarselType) {
    executeBatchUpdateQuery("""UPDATE ${VarselTable.fromVarselType(varselType)} SET aktiv = FALSE, frist_utløpt= FALSE, sistoppdatert = ? WHERE eventId = ? AND aktiv= TRUE""") {
        doneEvents.forEach { done ->
            setObject(1, LocalDateTimeHelper.nowAtUtc(), Types.TIMESTAMP)
            setString(2, done.eventId)
            addBatch()
        }
    }
}

private fun ResultSet.toVarsel(): VarselHeader {
    return VarselHeader(
        eventId = getString("eventId"),
        type = VarselType.valueOf(getString("type").uppercase()),
        fodselsnummer = getString("fodselsnummer"),
        namespace = getString("namespace"),
        appnavn = getString("appnavn")
    )
}
