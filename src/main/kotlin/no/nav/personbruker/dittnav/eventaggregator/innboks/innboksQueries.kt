package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.executePersistQuery
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.singleResult
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Types
import java.time.LocalDateTime
import java.time.ZoneId

fun Connection.getAllInnboks(): List<Innboks> =
        prepareStatement("""SELECT * FROM INNBOKS""")
                .use {
                    it.executeQuery().list {
                        toInnboks()
                    }
                }

fun Connection.getInnboksById(entityId: Int): Innboks =
        prepareStatement("""SELECT * FROM INNBOKS WHERE id = ?""")
                .use {
                    it.setInt(1, entityId)
                    it.executeQuery().singleResult {
                        toInnboks()
                    }
                }

fun Connection.createInnboks(innboks: Innboks): PersistActionResult =
        executePersistQuery("""INSERT INTO INNBOKS(produsent, eventTidspunkt, fodselsnummer, eventId, grupperingsId, tekst, link, sikkerhetsnivaa, sistOppdatert, aktiv)
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

fun Connection.setInnboksAktivFlag(eventId: String, aktiv: Boolean): Int =
        prepareStatement("""UPDATE INNBOKS SET aktiv = ? WHERE eventid = ?""")
            .use {
                it.setBoolean(1, aktiv)
                it.setString(2, eventId)
                it.executeUpdate()
            }

fun Connection.getAllInnboksByAktiv(aktiv: Boolean): List<Innboks> =
        prepareStatement("""SELECT * FROM INNBOKS WHERE aktiv = ?""")
                .use {
                    it.setBoolean(1, aktiv)
                    it.executeQuery().list {
                        toInnboks()
                    }
                }

fun Connection.getInnboksByFodselsnummer(fodselsnummer: String) : List<Innboks> =
        prepareStatement("""SELECT * FROM INNBOKS WHERE fodselsnummer = ?""")
                .use {
                    it.setString(1, fodselsnummer)
                    it.executeQuery().list {
                        toInnboks()
                    }
                }

fun Connection.getInnboksByEventId(eventId: String) : Innboks =
        prepareStatement("""SELECT * FROM INNBOKS WHERE eventId = ?""")
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
            eventTidspunkt = LocalDateTime.ofInstant(getTimestamp("eventTidspunkt").toInstant(), ZoneId.of("Europe/Oslo")),
            fodselsnummer = getString("fodselsnummer"),
            eventId = getString("eventId"),
            grupperingsId = getString("grupperingsId"),
            tekst = getString("tekst"),
            link = getString("link"),
            sikkerhetsnivaa = getInt("sikkerhetsnivaa"),
            sistOppdatert = LocalDateTime.ofInstant(getTimestamp("sistOppdatert").toInstant(), ZoneId.of("Europe/Oslo")),
            aktiv = getBoolean("aktiv")
    )
}