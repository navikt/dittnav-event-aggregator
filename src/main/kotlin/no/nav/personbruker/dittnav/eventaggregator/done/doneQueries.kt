package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.database.util.getUtcDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.getEpochTimeInSeconds
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Types
import java.time.LocalDateTime
import java.time.ZoneId

fun Connection.getAllDoneEvent(): List<Done> =
        prepareStatement("""SELECT * FROM Done""")
                .use {
                    it.executeQuery().list {
                        toDoneEvent()
                    }
                }

fun Connection.createDoneEvent(done: Done): Int =
        prepareStatement("""INSERT INTO DONE(produsent, eventTidspunkt, fodselsnummer, eventId, grupperingsId)
            VALUES (?, ?, ?, ?, ?)""", Statement.RETURN_GENERATED_KEYS).use {
            it.setString(1, done.produsent)
            it.setObject(2, done.eventTidspunkt, Types.TIMESTAMP)
            it.setString(3, done.fodselsnummer)
            it.setString(4, done.eventId)
            it.setString(5, done.grupperingsId)
            it.executeUpdate()
            it.generatedKeys.next()
            it.generatedKeys.getInt("id")
        }

private fun ResultSet.toDoneEvent(): Done {
    return Done(
            eventId = getString("eventId"),
            produsent = getString("produsent"),
            eventTidspunkt = getUtcDateTime("eventTidspunkt"),
            fodselsnummer = getString("fodselsnummer"),
            grupperingsId = getString("grupperingsId")
    )
}
