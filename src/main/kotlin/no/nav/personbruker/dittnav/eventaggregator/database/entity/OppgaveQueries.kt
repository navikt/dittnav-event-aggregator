package no.nav.personbruker.dittnav.eventaggregator.database.entity

import java.sql.*
import java.time.LocalDateTime
import java.time.ZoneId

fun Connection.getAllOppgave(): List<Oppgave> =
        prepareStatement("""SELECT * FROM OPPGAVE""")
                .use {
                    it.executeQuery().list{
                        toOppgave()
                    }
                }

fun Connection.createOppgave(oppgave: Oppgave) : Int =
        prepareStatement("""INSERT INTO OPPGAVE (produsent, eventTidspunkt, aktoerId, eventId, dokumentId, tekst, link, sikkerhetsnivaa) VALUES (?, ?, ?, ?, ?, ?, ?, ?)""",
                Statement.RETURN_GENERATED_KEYS)
                .use {
            it.setString(1, oppgave.produsent)
            it.setObject(2, oppgave.eventTidspunkt, Types.TIMESTAMP)
            it.setString(3, oppgave.aktoerId)
            it.setString(4, oppgave.eventId)
            it.setString(5, oppgave.dokumentId)
            it.setString(6, oppgave.tekst)
            it.setString(7, oppgave.link)
            it.setInt(8, oppgave.sikkerhetsinvaa)
            it.executeUpdate()
            it.generatedKeys.next()
            it.generatedKeys.getInt("id")
        }

fun Connection.getOppgaveByAktoerId(aktoerId: String) : List<Oppgave> =
        prepareStatement("""SELECT * FROM OPPGAVE WHERE aktoerId = ?""")
                .use {
                    it.setString(1,aktoerId)
                    it.executeQuery().list {
                        toOppgave()
                    }
                }

fun Connection.getOppgaveById(id: Int) : Oppgave =
        prepareStatement("""SELECT * FROM OPPGAVE WHERE id = ?""")
                .use {
                    it.setInt(1,id)
                    it.executeQuery().singleResult {
                        toOppgave()
                    }
                }

private fun ResultSet.toOppgave(): Oppgave {
    return Oppgave(
            id = getInt("id"),
            produsent = getString("produsent"),
            eventTidspunkt = LocalDateTime.ofInstant(getTimestamp("eventTidspunkt").toInstant(), ZoneId.of("Europe/Oslo")),
            aktoerId = getString("aktoerId"),
            eventId = getString("eventId"),
            dokumentId = getString("eventId"),
            tekst = getString("tekst"),
            link = getString("link"),
            sikkerhetsinvaa = getInt("sikkerhetsnivaa")
    )
}

private fun <T> ResultSet.singleResult(result: ResultSet.() -> T): T =
        if (next()) {
            result()
        } else {
            throw SQLException("Found no rows")
        }

private fun <T> ResultSet.list(result: ResultSet.() -> T): List<T> =
        mutableListOf<T>().apply {
            while (next()) {
                add(result())
            }
        }

