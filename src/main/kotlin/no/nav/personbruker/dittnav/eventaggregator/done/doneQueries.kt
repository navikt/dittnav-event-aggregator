package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.database.util.executeBatchUpdateQuery
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.getUtcDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types

private const val allDoneQuery = "SELECT * FROM done"

fun Connection.getAllDoneEvent(): List<Done> =
        prepareStatement(allDoneQuery)
                .use {
                    it.executeQuery().list {
                        toDoneEvent()
                    }
                }

fun Connection.getAllDoneEventWithLimit(limit: Int): List<Done> =
        prepareStatement("$allDoneQuery LIMIT ?")
                .use { pStatement ->
                    pStatement.setInt(1, limit)
                    pStatement.executeQuery().list {
                        toDoneEvent()
                    }
                }

fun Connection.createDoneEvents(doneEvents: List<Done>) {
    executeBatchUpdateQuery("""INSERT INTO done(systembruker, eventTidspunkt, fodselsnummer, eventId, grupperingsId)
            VALUES (?, ?, ?, ?, ?)""") {
        doneEvents.forEach { done ->
            setString(1, done.systembruker)
            setObject(2, done.eventTidspunkt, Types.TIMESTAMP)
            setString(3, done.fodselsnummer)
            setString(4, done.eventId)
            setString(5, done.grupperingsId)
            addBatch()
        }
    }
}

fun Connection.deleteDoneEvents(doneEvents: List<Done>) {
    executeBatchUpdateQuery("""DELETE FROM done WHERE eventId = ? AND systembruker = ? AND fodselsnummer = ?""") {
        doneEvents.forEach { done ->
            setString(1, done.eventId)
            setString(2, done.systembruker)
            setString(3, done.fodselsnummer)
            addBatch()
        }
    }
}

private fun ResultSet.toDoneEvent(): Done {
    return Done(
            eventId = getString("eventId"),
            systembruker = getString("systembruker"),
            eventTidspunkt = getUtcDateTime("eventTidspunkt"),
            fodselsnummer = getString("fodselsnummer"),
            grupperingsId = getString("grupperingsId")
    )
}
