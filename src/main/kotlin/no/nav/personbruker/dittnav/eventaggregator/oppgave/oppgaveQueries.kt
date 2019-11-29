package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.singleResult
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Types
import java.time.LocalDateTime
import java.time.ZoneId

fun Connection.getAllOppgave(): List<Oppgave> =
        prepareStatement("""SELECT * FROM OPPGAVE""")
                .use {
                    it.executeQuery().list {
                        toOppgave()
                    }
                }

fun Connection.createOppgave(oppgave: Oppgave): Int =
        prepareStatement("""INSERT INTO OPPGAVE (produsent, eventTidspunkt, fodselsnummer, eventId, grupperingsId, tekst, link, sikkerhetsnivaa, sistOppdatert, aktiv) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ? ,?)""",
                Statement.RETURN_GENERATED_KEYS)
                .use {
                    it.setString(1, oppgave.produsent)
                    it.setObject(2, oppgave.eventTidspunkt, Types.TIMESTAMP)
                    it.setString(3, oppgave.fodselsnummer)
                    it.setString(4, oppgave.eventId)
                    it.setString(5, oppgave.grupperingsId)
                    it.setString(6, oppgave.tekst)
                    it.setString(7, oppgave.link)
                    it.setInt(8, oppgave.sikkerhetsinvaa)
                    it.setObject(9, oppgave.sistOppdatert, Types.TIMESTAMP)
                    it.setBoolean(10, oppgave.aktiv)
                    it.executeUpdate()
                    it.generatedKeys.next()
                    it.generatedKeys.getInt("id")
                }

fun Connection.setOppgaveAktivFlag(eventId: String, aktiv: Boolean): Int =
        prepareStatement("""UPDATE OPPGAVE SET aktiv = ? WHERE eventId = ?""").use {
            it.setBoolean(1, aktiv)
            it.setString(2, eventId)
            it.executeUpdate()
        }

fun Connection.getAllOppgaveByAktiv(aktiv: Boolean): List<Oppgave> =
        prepareStatement("""SELECT * FROM OPPGAVE WHERE aktiv = ?""")
                .use {
                    it.setBoolean(1, aktiv)
                    it.executeQuery().list {
                        toOppgave()
                    }
                }

fun Connection.getOppgaveByFodselsnummer(fodselsnummer: String): List<Oppgave> =
        prepareStatement("""SELECT * FROM OPPGAVE WHERE fodselsnummer = ?""")
                .use {
                    it.setString(1, fodselsnummer)
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

fun Connection.getOppgaveByEventId(eventId: String): Oppgave =
        prepareStatement("""SELECT * FROM OPPGAVE WHERE eventId = ?""")
                .use {
                    it.setString(1, eventId)
                    it.executeQuery().singleResult {
                        toOppgave()
                    }
                }

private fun ResultSet.toOppgave(): Oppgave {
    return Oppgave(
            id = getInt("id"),
            produsent = getString("produsent"),
            eventTidspunkt = LocalDateTime.ofInstant(getTimestamp("eventTidspunkt").toInstant(), ZoneId.of("Europe/Oslo")),
            fodselsnummer = getString("fodselsnummer"),
            eventId = getString("eventId"),
            grupperingsId = getString("grupperingsId"),
            tekst = getString("tekst"),
            link = getString("link"),
            sikkerhetsinvaa = getInt("sikkerhetsnivaa"),
            sistOppdatert = LocalDateTime.ofInstant(getTimestamp("sistOppdatert").toInstant(), ZoneId.of("Europe/Oslo")),
            aktiv = getBoolean("aktiv")
    )
}
