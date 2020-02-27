package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.executePersistQuery
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.singleResult
import java.sql.Connection
import java.sql.ResultSet
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

fun Connection.createBeskjed(beskjed: Beskjed): PersistActionResult =
        executePersistQuery("""INSERT INTO BESKJED (uid, produsent, eventTidspunkt, fodselsnummer, eventId, grupperingsId, tekst, link, sikkerhetsnivaa, sistOppdatert, synligFremTil, aktiv)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""") {
            setString(1, beskjed.uid)
            setString(2, beskjed.produsent)
            setObject(3, beskjed.eventTidspunkt, Types.TIMESTAMP)
            setString(4, beskjed.fodselsnummer)
            setString(5, beskjed.eventId)
            setString(6, beskjed.grupperingsId)
            setString(7, beskjed.tekst)
            setString(8, beskjed.link)
            setInt(9, beskjed.sikkerhetsnivaa)
            setObject(10, beskjed.sistOppdatert, Types.TIMESTAMP)
            setObject(11, beskjed.synligFremTil, Types.TIMESTAMP)
            setBoolean(12, beskjed.aktiv)
        }

fun Connection.setBeskjedAktivFlag(eventId: String, produsent: String, fodselsnummer: String, aktiv: Boolean): Int =
        prepareStatement("""UPDATE BESKJED SET aktiv = ? WHERE eventId = ? AND produsent = ? AND fodselsnummer = ?""").use {
            it.setBoolean(1, aktiv)
            it.setString(2, eventId)
            it.setString(3, produsent)
            it.setString(4, fodselsnummer)
            it.executeUpdate()
        }

fun Connection.getAllBeskjedByAktiv(aktiv: Boolean): List<Beskjed> =
        prepareStatement("""SELECT * FROM BESKJED WHERE aktiv = ?""")
                .use {
                    it.setBoolean(1, aktiv)
                    it.executeQuery().list {
                        toBeskjed()
                    }
                }

fun Connection.getBeskjedByFodselsnummer(fodselsnummer: String): List<Beskjed> =
        prepareStatement("""SELECT * FROM BESKJED WHERE fodselsnummer = ?""")
                .use {
                    it.setString(1, fodselsnummer)
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
            uid = getNullableUid("uid"),
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
            synligFremTil = getNullableLocalDateTime("synligFremTil"),
            aktiv = getBoolean("aktiv")
    )
}

private fun ResultSet.getNullableLocalDateTime(label: String): LocalDateTime? {
    return getTimestamp(label)?.let { timestamp -> LocalDateTime.ofInstant(timestamp.toInstant(), ZoneId.of("Europe/Oslo")) }
}

private fun ResultSet.getNullableUid(label: String): String {
    if (getString(label).isNullOrBlank()) {
        return "0"
    } else {
        return getString(label)
    }
}
