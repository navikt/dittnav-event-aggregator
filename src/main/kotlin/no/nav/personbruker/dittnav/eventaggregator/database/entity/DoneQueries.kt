package no.nav.personbruker.dittnav.eventaggregator.database.entity

import java.sql.Connection
import java.sql.Statement
import java.sql.Types

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
