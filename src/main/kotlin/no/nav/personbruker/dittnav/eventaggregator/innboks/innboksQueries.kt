package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.executePersistQuery
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.getUtcDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.singleResult
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types

fun Connection.getAllInnboks(): List<Innboks> =
        prepareStatement("""SELECT * FROM innboks""")
                .use {
                    it.executeQuery().list {
                        toInnboks()
                    }
                }

fun Connection.getInnboksById(entityId: Int): Innboks =
        prepareStatement("""SELECT * FROM innboks WHERE id = ?""")
                .use {
                    it.setInt(1, entityId)
                    it.executeQuery().singleResult {
                        toInnboks()
                    }
                }

fun Connection.createInnboks(innboks: Innboks): PersistActionResult =
        executePersistQuery("""INSERT INTO innboks(produsent, eventTidspunkt, fodselsnummer, eventId, grupperingsId, tekst, link, sikkerhetsnivaa, sistOppdatert, aktiv)
            VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""") {
            setString(1, innboks.produsent)
            setObject(2, innboks.eventTidspunkt, Types.TIMESTAMP)
            setString(3, innboks.fodselsnummer)
            setString(4, innboks.eventId)
            setString(5, innboks.grupperingsId)
            setString(6, innboks.tekst)
            setString(7, innboks.link)
            setInt(8, innboks.sikkerhetsnivaa)
            setObject(9, innboks.sistOppdatert, Types.TIMESTAMP)
            setBoolean(10, innboks.aktiv)
        }

fun Connection.setInnboksAktivFlag(eventId: String, produsent: String, fodselsnummer: String, aktiv: Boolean): Int =
        prepareStatement("""UPDATE innboks SET aktiv = ? WHERE eventId = ? AND produsent = ? AND fodselsnummer = ?""")
                .use {
                    it.setBoolean(1, aktiv)
                    it.setString(2, eventId)
                    it.setString(3, produsent)
                    it.setString(4, fodselsnummer)
                    it.executeUpdate()
                }

fun Connection.getAllInnboksByAktiv(aktiv: Boolean): List<Innboks> =
        prepareStatement("""SELECT * FROM innboks WHERE aktiv = ?""")
                .use {
                    it.setBoolean(1, aktiv)
                    it.executeQuery().list {
                        toInnboks()
                    }
                }

fun Connection.getInnboksByFodselsnummer(fodselsnummer: String): List<Innboks> =
        prepareStatement("""SELECT * FROM innboks WHERE fodselsnummer = ?""")
                .use {
                    it.setString(1, fodselsnummer)
                    it.executeQuery().list {
                        toInnboks()
                    }
                }

fun Connection.getInnboksByEventId(eventId: String): Innboks =
        prepareStatement("""SELECT * FROM innboks WHERE eventId = ?""")
                .use {
                    it.setString(1, eventId)
                    it.executeQuery().singleResult {
                        toInnboks()
                    }
                }


private fun ResultSet.toInnboks(): Innboks {
    return Innboks(
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