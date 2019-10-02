package no.nav.personbruker.dittnav.eventaggregator.database.entity

import no.nav.personbruker.dittnav.eventaggregator.database.util.list
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Types
import java.time.LocalDateTime
import java.time.ZoneId

fun Connection.getAllDone(): List<Done> =
        prepareStatement("""SELECT * FROM Done""")
                .use {
                    it.executeQuery().list {
                        toDone()
                    }
                }

fun Connection.createDone(done: Done): Int =
        prepareStatement("""INSERT INTO DONE(produsent, eventTidspunkt, aktorid, eventId, dokumentId)
            VALUES (?, ?, ?, ?, ?)""", Statement.RETURN_GENERATED_KEYS).use {
            it.setString(1, done.produsent)
            it.setObject(2, done.eventTidspunkt, Types.TIMESTAMP)
            it.setString(3, done.aktorId)
            it.setString(4, done.eventId)
            it.setString(5, done.dokumentId)
            it.executeUpdate()
            it.generatedKeys.next()
            it.generatedKeys.getInt("id")
        }

private fun ResultSet.toDone(): Done {
    return Done(
            eventId = getString("eventId"),
            produsent = getString("produsent"),
            eventTidspunkt = LocalDateTime.ofInstant(getTimestamp("eventTidspunkt").toInstant(), ZoneId.of("Europe/Oslo")),
            aktorId = getString("aktorId"),
            dokumentId = getString("dokumentId")
    )
}
