package no.nav.personbruker.dittnav.eventaggregator.database.query

import no.nav.personbruker.dittnav.eventaggregator.database.entity.Informasjon
import java.sql.*


fun Connection.getAllInfo() =
        prepareStatement("""SELECT * FROM INFORMASJON""")
                .use {
                    it.executeQuery().list {
                        toInformasjon()
                    }
                }

fun Connection.createInfo(informasjon: Informasjon): Int =
        prepareStatement("""INSERT INTO INFORMASJON (produsent, eventTidspunkt, aktorid, eventId, dokumentId, tekst, link, sikkerhetsnivaa, sistOppdatert, aktiv)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""", Statement.RETURN_GENERATED_KEYS).use {
            it.setString(1, informasjon.produsent)
            it.setObject(2, informasjon.eventTidspunkt, Types.DATE)
            it.setString(3, informasjon.aktorId)
            it.setString(4, informasjon.eventId)
            it.setString(5, informasjon.dokumentId)
            it.setString(6, informasjon.tekst)
            it.setString(7, informasjon.link)
            it.setInt(8, informasjon.sikkerhetsnivaa)
            it.setObject(9, informasjon.sistOppdatert, Types.DATE)
            it.setBoolean(10, informasjon.aktiv)
            it.executeUpdate()
            it.generatedKeys.next()
            it.generatedKeys.getInt("id")
        }

fun Connection.getInformasjonByAktorid(aktorid: String): Informasjon? =
        prepareStatement("""SELECT * FROM INFORMASJON WHERE aktorid = ?""")
                .use {
                    it.setString(1, aktorid)
                    it.executeQuery().singleResult {
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
            eventTidspunkt = getDate("eventTidspunkt"),
            aktorId = getString("aktorId"),
            eventId = getString("eventId"),
            dokumentId = getString("dokumentId"),
            tekst = getString("tekst"),
            link = getString("link"),
            sikkerhetsnivaa = getInt("sikkerhetsnivaa"),
            sistOppdatert = getDate("sistOppdatert"),
            aktiv = getBoolean("aktiv")
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
