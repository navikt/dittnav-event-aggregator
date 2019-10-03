package no.nav.personbruker.dittnav.eventaggregator.database.entity

import java.sql.*
import java.time.LocalDateTime
import java.time.ZoneId
import no.nav.personbruker.dittnav.eventaggregator.database.util.list
import no.nav.personbruker.dittnav.eventaggregator.database.util.singleResult

fun Connection.getAllInformasjon(): List<Informasjon> =
        prepareStatement("""SELECT * FROM INFORMASJON""")
                .use {
                    it.executeQuery().list {
                        toInformasjon()
                    }
                }

fun Connection.createInformasjon(informasjon: Informasjon): Int =
        prepareStatement("""INSERT INTO INFORMASJON (produsent, eventTidspunkt, aktorid, eventId, dokumentId, tekst, link, sikkerhetsnivaa, sistOppdatert, aktiv)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""", Statement.RETURN_GENERATED_KEYS).use {
            it.setString(1, informasjon.produsent)
            it.setObject(2, informasjon.eventTidspunkt, Types.TIMESTAMP)
            it.setString(3, informasjon.aktorId)
            it.setString(4, informasjon.eventId)
            it.setString(5, informasjon.dokumentId)
            it.setString(6, informasjon.tekst)
            it.setString(7, informasjon.link)
            it.setInt(8, informasjon.sikkerhetsnivaa)
            it.setObject(9, informasjon.sistOppdatert, Types.TIMESTAMP)
            it.setBoolean(10, informasjon.aktiv)
            it.executeUpdate()
            it.generatedKeys.next()
            it.generatedKeys.getInt("id")
        }

fun Connection.setInformasjonAktiv(eventId: String, aktiv: Boolean): Int =
        prepareStatement("""UPDATE INFORMASJON SET aktiv = ? WHERE eventId = ?""").use {
            it.setBoolean(1, aktiv)
            it.setString(2, eventId)
            it.executeUpdate()
        }

fun Connection.getInformasjonByAktorId(aktorId: String): List<Informasjon> =
        prepareStatement("""SELECT * FROM INFORMASJON WHERE aktorId = ?""")
                .use {
                    it.setString(1, aktorId)
                    it.executeQuery().list {
                        toInformasjon()
                    }
                }

fun Connection.getInformasjonById(id: Int): Informasjon? =
        prepareStatement("""SELECT * FROM INFORMASJON WHERE id = ?""")
                .use {
                    it.setInt(1, id)
                    it.executeQuery().singleResult() {
                        toInformasjon()
                    }
                }

private fun ResultSet.toInformasjon(): Informasjon {
    return Informasjon(
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
