package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.executeBatchUpdateQuery
import no.nav.personbruker.dittnav.eventaggregator.common.database.executePersistQuery
import no.nav.personbruker.dittnav.eventaggregator.common.database.getUtcDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import no.nav.personbruker.dittnav.eventaggregator.common.database.toVarcharArray
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselHeader
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import java.time.LocalDateTime

private const val allDoneQuery = "SELECT * FROM done ORDER BY sistBehandlet"

fun Connection.getAllDoneEventWithLimit(limit: Int): List<Done> =
        prepareStatement("$allDoneQuery LIMIT ?")
                .use { pStatement ->
                    pStatement.setInt(1, limit)
                    pStatement.executeQuery().list {
                        toDoneEvent()
                    }
                }

private const val createQuery = """INSERT INTO done(systembruker, eventTidspunkt, forstbehandlet, fodselsnummer, eventId, grupperingsId, namespace, appnavn, sistBehandlet)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"""

fun Connection.createDoneEvent(doneEvent: Done) : PersistActionResult =
        executePersistQuery(createQuery) {
            buildStatementForSingleRow(doneEvent)
        }

fun Connection.deleteDoneEvents(doneEvents: List<Done>) {
    executeBatchUpdateQuery("""DELETE FROM done WHERE eventId = ?""") {
        doneEvents.forEach { done ->
            setString(1, done.eventId)
            addBatch()
        }
    }
}

fun Connection.updateDoneSistbehandlet(doneEvents: List<Done>, sistBehandlet: LocalDateTime) {
    executeBatchUpdateQuery("""UPDATE done SET sistBehandlet = ? WHERE eventId = ?""") {
        doneEvents.forEach { done ->
            setObject(1, sistBehandlet, Types.TIMESTAMP)
            setString(2, done.eventId)
            addBatch()
        }
    }
}

fun ResultSet.toDoneEvent(): Done {
    return Done(
            eventId = getString("eventId"),
            systembruker = getString("systembruker"),
            namespace = getString("namespace"),
            appnavn = getString("appnavn"),
            eventTidspunkt = getUtcDateTime("eventTidspunkt"),
            forstBehandlet = getUtcDateTime("forstBehandlet"),
            fodselsnummer = getString("fodselsnummer"),
            grupperingsId = getString("grupperingsId"),
            sistBehandlet = getUtcDateTime("sistBehandlet")
    )
}

fun Connection.getBrukernotifikasjonFromViewForEventIds(eventIds: List<String>): List<VarselHeader> =
    prepareStatement("""SELECT * FROM varsel_header_view WHERE eventid = ANY(?)""")
        .use {
            it.setArray(1, toVarcharArray(eventIds))
            it.executeQuery().list {
                toVarselHeader()
            }
        }

private fun ResultSet.toVarselHeader(): VarselHeader {
    return VarselHeader(
        eventId = getString("eventId"),
        appnavn = getString("appnavn"),
        aktiv = getBoolean("aktiv"),
        namespace = getString("namespace"),
        type = VarselType.valueOf(getString("type").uppercase()),
        fodselsnummer = getString("fodselsnummer")
    )
}

private fun PreparedStatement.buildStatementForSingleRow(doneEvent: Done) {
    setString(1, doneEvent.systembruker)
    setObject(2, doneEvent.eventTidspunkt, Types.TIMESTAMP)
    setObject(3, doneEvent.forstBehandlet, Types.TIMESTAMP)
    setString(4, doneEvent.fodselsnummer)
    setString(5, doneEvent.eventId)
    setString(6, doneEvent.grupperingsId)
    setString(7, doneEvent.namespace)
    setString(8, doneEvent.appnavn)
    setObject(9, doneEvent.sistBehandlet, Types.TIMESTAMP)
}

