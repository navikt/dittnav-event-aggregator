package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.*
import java.sql.Connection
import java.sql.PreparedStatement
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

private const val createQuery = """INSERT INTO done(systembruker, eventTidspunkt, fodselsnummer, eventId, grupperingsId)
            VALUES (?, ?, ?, ?, ?)"""

fun Connection.createDoneEvents(doneEvents: List<Done>): ListPersistActionResult<Done> =
    executeBatchPersistQuery(createQuery) {
        doneEvents.forEach { done ->
            buildStatementForSingleRow(done)
            addBatch()
        }
    }.toBatchPersistResult(doneEvents)

fun Connection.createDoneEvent(doneEvent: Done) : PersistActionResult =
        executePersistQuery(createQuery) {
            buildStatementForSingleRow(doneEvent)
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

private fun PreparedStatement.buildStatementForSingleRow(doneEvent: Done) {
    setString(1, doneEvent.systembruker)
    setObject(2, doneEvent.eventTidspunkt, Types.TIMESTAMP)
    setString(3, doneEvent.fodselsnummer)
    setString(4, doneEvent.eventId)
    setString(5, doneEvent.grupperingsId)
}
