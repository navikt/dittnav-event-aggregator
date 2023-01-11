package no.nav.personbruker.dittnav.eventaggregator.varsel

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper
import no.nav.personbruker.dittnav.eventaggregator.common.database.executeBatchUpdateQuery
import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import no.nav.personbruker.dittnav.eventaggregator.common.database.toVarcharArray
import no.nav.personbruker.dittnav.eventaggregator.done.Done
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types


fun Connection.getVarsler(eventIds: List<String>): List<VarselIdentifier> =
    prepareStatement("""SELECT brukernotifikasjon_view.* FROM brukernotifikasjon_view WHERE eventid = ANY(?)""")
        .use {
            it.setArray(1, toVarcharArray(eventIds))
            it.executeQuery().list {
                toVarsel()
            }
        }

fun Connection.setVarselInaktiv(eventId: String, varselType: VarselType): Int =
    prepareStatement("""UPDATE ${VarselTable.fromVarselType(varselType)} SET aktiv = FALSE, frist_utløpt= FALSE, sistoppdatert = ? WHERE eventId = ? AND aktiv=TRUE""".trimMargin())
        .use {
            it.setObject(1, LocalDateTimeHelper.nowAtUtc(), Types.TIMESTAMP)
            it.setString(2, eventId)
            it.executeUpdate()
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

private fun ResultSet.toVarsel(): VarselIdentifier {
    return VarselIdentifier(
        eventId = getString("eventId"),
        systembruker = getString("systembruker"),
        type = VarselType.valueOf(getString("type").uppercase()),
        fodselsnummer = getString("fodselsnummer")
    )
}