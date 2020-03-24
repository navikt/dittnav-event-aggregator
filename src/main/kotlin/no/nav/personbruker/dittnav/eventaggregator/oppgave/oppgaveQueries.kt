package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.executePersistQuery
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.getUtcDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.singleResult
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types

fun Connection.getAllOppgave(): List<Oppgave> =
        prepareStatement("""SELECT * FROM ytest_oppgave""")
                .use {
                    it.executeQuery().list {
                        toOppgave()
                    }
                }

fun Connection.createOppgave(oppgave: Oppgave): PersistActionResult =
        executePersistQuery("""INSERT INTO ytest_oppgave (produsent, eventTidspunkt, fodselsnummer, eventId, grupperingsId, tekst, link, sikkerhetsnivaa, sistOppdatert, aktiv) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ? ,?)""")
        {
            setString(1, oppgave.produsent)
            setObject(2, oppgave.eventTidspunkt, Types.TIMESTAMP)
            setString(3, oppgave.fodselsnummer)
            setString(4, oppgave.eventId)
            setString(5, oppgave.grupperingsId)
            setString(6, oppgave.tekst)
            setString(7, oppgave.link)
            setInt(8, oppgave.sikkerhetsnivaa)
            setObject(9, oppgave.sistOppdatert, Types.TIMESTAMP)
            setBoolean(10, oppgave.aktiv)
        }

fun Connection.setOppgaveAktivFlag(eventId: String, produsent: String, fodselsnummer: String, aktiv: Boolean): Int =
        prepareStatement("""UPDATE ytest_oppgave SET aktiv = ? WHERE eventId = ? AND produsent = ? AND fodselsnummer = ?""").use {
            it.setBoolean(1, aktiv)
            it.setString(2, eventId)
            it.setString(3, produsent)
            it.setString(4, fodselsnummer)
            it.executeUpdate()
        }

fun Connection.getAllOppgaveByAktiv(aktiv: Boolean): List<Oppgave> =
        prepareStatement("""SELECT * FROM ytest_oppgave WHERE aktiv = ?""")
                .use {
                    it.setBoolean(1, aktiv)
                    it.executeQuery().list {
                        toOppgave()
                    }
                }

fun Connection.getOppgaveByFodselsnummer(fodselsnummer: String): List<Oppgave> =
        prepareStatement("""SELECT * FROM ytest_oppgave WHERE fodselsnummer = ?""")
                .use {
                    it.setString(1, fodselsnummer)
                    it.executeQuery().list {
                        toOppgave()
                    }
                }

fun Connection.getOppgaveById(id: Int): Oppgave =
        prepareStatement("""SELECT * FROM ytest_oppgave WHERE id = ?""")
                .use {
                    it.setInt(1, id)
                    it.executeQuery().singleResult {
                        toOppgave()
                    }
                }

fun Connection.getOppgaveByEventId(eventId: String): Oppgave =
        prepareStatement("""SELECT * FROM ytest_oppgave WHERE eventId = ?""")
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
            eventTidspunkt = getUtcDateTime("eventTidspunkt"),
            fodselsnummer = getString("fodselsnummer"),
            eventId = getString("eventId"),
            grupperingsId = getString("grupperingsId"),
            tekst = getString("tekst"),
            link = getString("link"),
            sikkerhetsnivaa = getInt("sikkerhetsnivaa"),
            sistOppdatert = getUtcDateTime("sistOppdatert"),
            aktiv = getBoolean("aktiv")
    )
}
