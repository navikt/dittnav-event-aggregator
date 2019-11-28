package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.singleResult
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Types
import java.time.LocalDateTime
import java.time.ZoneId

fun Connection.getAllBeskjed(): List<Beskjed> =
        prepareStatement("""SELECT * FROM BESKJED""")
                .use {
                    it.executeQuery().list {
                        toBeskjed()
                    }
                }

fun Connection.createBeskjed(beskjed: Beskjed): Int =
        prepareStatement("""INSERT INTO BESKJED (produsent, eventTidspunkt, aktorid, eventId, dokumentId, tekst, link, sikkerhetsnivaa, sistOppdatert, aktiv)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""", Statement.RETURN_GENERATED_KEYS).use {
            it.setString(1, beskjed.produsent)
            it.setObject(2, beskjed.eventTidspunkt, Types.TIMESTAMP)
            it.setString(3, beskjed.aktorId)
            it.setString(4, beskjed.eventId)
            it.setString(5, beskjed.dokumentId)
            it.setString(6, beskjed.tekst)
            it.setString(7, beskjed.link)
            it.setInt(8, beskjed.sikkerhetsnivaa)
            it.setObject(9, beskjed.sistOppdatert, Types.TIMESTAMP)
            it.setBoolean(10, beskjed.aktiv)
            it.executeUpdate()
            it.generatedKeys.next()
            it.generatedKeys.getInt("id")
        }

fun Connection.setBeskjedAktivFlag(eventId: String, aktiv: Boolean): Int =
        prepareStatement("""UPDATE BESKJED SET aktiv = ? WHERE eventId = ?""").use {
            it.setBoolean(1, aktiv)
            it.setString(2, eventId)
            it.executeUpdate()
        }

fun Connection.getAllBeskjedByAktiv(aktiv: Boolean): List<Beskjed> =
        prepareStatement("""SELECT * FROM BESKJED WHERE aktiv = ?""")
                .use {
                    it.setBoolean(1,aktiv)
                    it.executeQuery().list {
                        toBeskjed()
                    }
                }

fun Connection.getBeskjedByAktorId(aktorId: String): List<Beskjed> =
        prepareStatement("""SELECT * FROM BESKJED WHERE aktorId = ?""")
                .use {
                    it.setString(1, aktorId)
                    it.executeQuery().list {
                        toBeskjed()
                    }
                }

fun Connection.getBeskjedById(id: Int): Beskjed =
        prepareStatement("""SELECT * FROM BESKJED WHERE id = ?""")
                .use {
                    it.setInt(1, id)
                    it.executeQuery().singleResult() {
                        toBeskjed()
                    }
                }

fun Connection.getBeskjedByEventId(eventId: String): Beskjed =
        prepareStatement("""SELECT * FROM BESKJED WHERE eventId = ?""")
                .use {
                    it.setString(1, eventId)
                    it.executeQuery().singleResult() {
                        toBeskjed()
                    }
                }

private fun ResultSet.toBeskjed(): Beskjed {
    return Beskjed(
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
