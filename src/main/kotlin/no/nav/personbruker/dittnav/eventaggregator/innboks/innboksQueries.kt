package no.nav.personbruker.dittnav.eventaggregator.innboks

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


fun Connection.createInnboks(innboks: Innboks): Int =
        prepareStatement("""INSERT INTO INNBOKS(produsent, eventTidspunkt, aktorId, eventId, dokumentId, tekst, link, sikkerhetsnivaa, sistOppdatert, aktiv)
            VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""", Statement.RETURN_GENERATED_KEYS).use {
            it.setString(1, innboks.produsent)
            it.setObject(2, innboks.eventTidspunkt, Types.TIMESTAMP)
            it.setString(3, innboks.aktorId)
            it.setString(4, innboks.eventId)
            it.setString(5, innboks.dokumentId)
            it.setString(6, innboks.tekst)
            it.setString(7, innboks.link)
            it.setInt(8, innboks.sikkerhetsnivaa)
            it.setObject(9, innboks.sistOppdatert, Types.TIMESTAMP)
            it.setBoolean(10, innboks.aktiv)
            it.executeUpdate()
            it.generatedKeys.next()
            it.generatedKeys.getInt("id")
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

fun Connection.getInnboksByAktorId(aktorId: String) : List<Innboks> =
        prepareStatement("""SELECT * FROM INNBOKS WHERE aktorId = ?""")
                .use {
                    it.setString(1, aktorId)
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
            aktorId = getString("aktorId"),
            eventId = getString("eventId"),
            dokumentId = getString("dokumentId"),
            tekst = getString("tekst"),
            link = getString("link"),
            sikkerhetsnivaa = getInt("sikkerhetsnivaa"),
            sistOppdatert = LocalDateTime.ofInstant(getTimestamp("sistOppdatert").toInstant(), ZoneId.of("Europe/Oslo")),
            aktiv = getBoolean("aktiv")
    )
}