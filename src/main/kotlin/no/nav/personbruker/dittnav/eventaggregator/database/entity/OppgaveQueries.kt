package no.nav.personbruker.dittnav.eventaggregator.database.entity

import java.sql.*
import java.time.LocalDateTime
import java.time.ZoneId
import no.nav.personbruker.dittnav.eventaggregator.database.util.list
import no.nav.personbruker.dittnav.eventaggregator.database.util.singleResult

fun Connection.getAllOppgave(): List<Oppgave> =
        prepareStatement("""SELECT * FROM OPPGAVE""")
                .use {
                    it.executeQuery().list {
                        toOppgave()
                    }
                }

fun Connection.createOppgave(oppgave: Oppgave): Int =
        prepareStatement("""INSERT INTO OPPGAVE (produsent, eventTidspunkt, aktorId, eventId, dokumentId, tekst, link, sikkerhetsnivaa, sistOppdatert, aktiv) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ? ,?)""",
                Statement.RETURN_GENERATED_KEYS)
                .use {
                    it.setString(1, oppgave.produsent)
                    it.setObject(2, oppgave.eventTidspunkt, Types.TIMESTAMP)
                    it.setString(3, oppgave.aktorId)
                    it.setString(4, oppgave.eventId)
                    it.setString(5, oppgave.dokumentId)
                    it.setString(6, oppgave.tekst)
                    it.setString(7, oppgave.link)
                    it.setInt(8, oppgave.sikkerhetsinvaa)
                    it.setObject(9, oppgave.sistOppdatert, Types.TIMESTAMP)
                    it.setBoolean(10, oppgave.aktiv)
                    it.executeUpdate()
                    it.generatedKeys.next()
                    it.generatedKeys.getInt("id")
                }

fun Connection.setOppgaveAktiv(eventId: String, aktiv: Boolean): Int =
        prepareStatement("""UPDATE OPPGAVE SET aktiv = ? WHERE eventId = ?""",
                Statement.RETURN_GENERATED_KEYS).use {
            it.setBoolean(1, aktiv)
            it.setString(2, eventId)
            it.executeUpdate()
            it.generatedKeys.next()
            it.generatedKeys.getInt("id")
        }

fun Connection.getOppgaveByAktorId(aktorId: String): List<Oppgave> =
        prepareStatement("""SELECT * FROM OPPGAVE WHERE aktorId = ?""")
                .use {
                    it.setString(1, aktorId)
                    it.executeQuery().list {
                        toOppgave()
                    }
                }

fun Connection.getOppgaveById(id: Int): Oppgave =
        prepareStatement("""SELECT * FROM OPPGAVE WHERE id = ?""")
                .use {
                    it.setInt(1, id)
                    it.executeQuery().singleResult {
                        toOppgave()
                    }
                }

private fun ResultSet.toOppgave(): Oppgave {
    return Oppgave(
            id = getInt("id"),
            produsent = getString("produsent"),
            eventTidspunkt = LocalDateTime.ofInstant(getTimestamp("eventTidspunkt").toInstant(), ZoneId.of("Europe/Oslo")),
            aktorId = getString("aktorId"),
            eventId = getString("eventId"),
            dokumentId = getString("dokumentId"),
            tekst = getString("tekst"),
            link = getString("link"),
            sikkerhetsinvaa = getInt("sikkerhetsnivaa"),
            sistOppdatert = LocalDateTime.ofInstant(getTimestamp("sistOppdatert").toInstant(), ZoneId.of("Europe/Oslo")),
            aktiv = getBoolean("aktiv")
    )
}
