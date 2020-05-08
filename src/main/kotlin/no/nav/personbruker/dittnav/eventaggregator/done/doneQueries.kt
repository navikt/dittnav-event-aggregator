package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.database.util.getUtcDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Types

fun Connection.getAllDoneEvent(): List<Done> =
        prepareStatement("""SELECT * FROM done""")
                .use {
                    it.executeQuery().list {
                        toDoneEvent()
                    }
                }

fun Connection.createDoneEvent(done: Done): Int =
        prepareStatement("""INSERT INTO done(systembruker, eventTidspunkt, fodselsnummer, eventId, grupperingsId)
            VALUES (?, ?, ?, ?, ?)""", Statement.RETURN_GENERATED_KEYS).use {
            it.setString(1, done.systembruker)
            it.setObject(2, done.eventTidspunkt, Types.TIMESTAMP)
            it.setString(3, done.fodselsnummer)
            it.setString(4, done.eventId)
            it.setString(5, done.grupperingsId)
            it.executeUpdate()
            it.generatedKeys.next()
            it.generatedKeys.getInt("id")
        }

fun Connection.deleteDoneEvent(doneEventToDelete: Done): Boolean =
        prepareStatement("""DELETE FROM done WHERE eventId = ? AND systembruker = ? AND fodselsnummer = ?""")
                .use {
                    it.setString(1, doneEventToDelete.eventId)
                    it.setString(2, doneEventToDelete.systembruker)
                    it.setString(3, doneEventToDelete.fodselsnummer)
                    it.execute()
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
