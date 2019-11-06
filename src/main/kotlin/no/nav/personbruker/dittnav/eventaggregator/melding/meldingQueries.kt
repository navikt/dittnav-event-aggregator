package no.nav.personbruker.dittnav.eventaggregator.melding

import net.logstash.logback.encoder.com.lmax.disruptor.LifecycleAware
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.singleResult
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Types
import java.time.LocalDateTime
import java.time.ZoneId

fun Connection.getAllMelding(): List<Melding> =
        prepareStatement("""SELECT * FROM MELDING""")
                .use {
                    it.executeQuery().list {
                        toMelding()
                    }
                }

fun Connection.getMeldingById(entityId: Int): Melding =
        prepareStatement("""SELECT * FROM MELDING WHERE id = ?""")
                .use {
                    it.setInt(1, entityId)
                    it.executeQuery().singleResult {
                        toMelding()
                    }
                }


fun Connection.createMelding(melding: Melding): Int =
        prepareStatement("""INSERT INTO MELDING(produsent, tidspunkt, aktorId, eventId, dokumentId, tekst, link, sikkerhetsnivaa, aktiv)
            VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)""", Statement.RETURN_GENERATED_KEYS).use {
            it.setString(1, melding.produsent)
            it.setObject(2, melding.tidspunkt, Types.TIMESTAMP)
            it.setString(3, melding.aktorId)
            it.setString(4, melding.eventId)
            it.setString(5, melding.dokumentId)
            it.setString(6, melding.tekst)
            it.setString(7, melding.link)
            it.setInt(8, melding.sikkerhetsnivaa)
            it.setBoolean(9, melding.aktiv)
            it.executeUpdate()
            it.generatedKeys.next()
            it.generatedKeys.getInt("id")
        }

fun Connection.setMeldingAktivFlag(eventId: String, aktiv: Boolean): Int =
        prepareStatement("""UPDATE MELDING SET aktiv = ? WHERE eventid = ?""")
            .use {
                it.setBoolean(1, aktiv)
                it.setString(2, eventId)
                it.executeUpdate()
            }

fun Connection.getAllMeldingByAktiv(aktiv: Boolean): List<Melding> =
        prepareStatement("""SELECT * FROM MELDING WHERE aktiv = ?""")
                .use {
                    it.setBoolean(1, aktiv)
                    it.executeQuery().list {
                        toMelding()
                    }
                }

fun Connection.getMeldingByAktorId(aktorId: String) : List<Melding> =
        prepareStatement("""SELECT * FROM MELDING WHERE aktorId = ?""")
                .use {
                    it.setString(1, aktorId)
                    it.executeQuery().list {
                        toMelding()
                    }
                }

fun Connection.getMeldingByEventId(eventId: String) : Melding =
        prepareStatement("""SELECT * FROM MELDING WHERE eventId = ?""")
                .use {
                    it.setString(1, eventId)
                    it.executeQuery().singleResult {
                        toMelding()
                    }
                }



private fun ResultSet.toMelding(): Melding {
    return Melding(
            id = getInt("id"),
            produsent = getString("produsent"),
            tidspunkt = LocalDateTime.ofInstant(getTimestamp("tidspunkt").toInstant(), ZoneId.of("Europe/Oslo")),
            aktorId = getString("aktorId"),
            eventId = getString("eventId"),
            dokumentId = getString("dokumentId"),
            tekst = getString("tekst"),
            link = getString("link"),
            sikkerhetsnivaa = getInt("sikkerhetsnivaa"),
            aktiv = getBoolean("aktiv")
    )
}