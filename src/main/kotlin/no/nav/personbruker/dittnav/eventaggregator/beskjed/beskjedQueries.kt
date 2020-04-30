package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.*
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import java.time.LocalDateTime

fun Connection.getAllBeskjed(): List<Beskjed> =
        prepareStatement("""SELECT * FROM beskjed""")
                .use {
                    it.executeQuery().list {
                        toBeskjed()
                    }
                }

private val createQuery = """INSERT INTO beskjed (uid, produsent, eventTidspunkt, fodselsnummer, eventId, grupperingsId, tekst, link, sikkerhetsnivaa, sistOppdatert, synligFremTil, aktiv)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

fun Connection.createBeskjed(beskjed: Beskjed): PersistActionResult =
        executePersistQuery(createQuery) {
            buildStatementForSingleRow(beskjed)
        }

fun Connection.createBeskjeder(beskjeder: List<Beskjed>) =
        executeBatchUpdateQuery(createQuery) {
            beskjeder.forEach { beskjed ->
                buildStatementForSingleRow(beskjed)
                addBatch()
            }
        }

private fun PreparedStatement.buildStatementForSingleRow(beskjed: Beskjed) {
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
        prepareStatement("""UPDATE beskjed SET aktiv = ? WHERE eventId = ? AND produsent = ? AND fodselsnummer = ?""").use {
            it.setBoolean(1, aktiv)
            it.setString(2, eventId)
            it.setString(3, produsent)
            it.setString(4, fodselsnummer)
            it.executeUpdate()
        }

fun Connection.getAllBeskjedByAktiv(aktiv: Boolean): List<Beskjed> =
        prepareStatement("""SELECT * FROM beskjed WHERE aktiv = ?""")
                .use {
                    it.setBoolean(1, aktiv)
                    it.executeQuery().list {
                        toBeskjed()
                    }
                }

fun Connection.getBeskjedByFodselsnummer(fodselsnummer: String): List<Beskjed> =
        prepareStatement("""SELECT * FROM beskjed WHERE fodselsnummer = ?""")
                .use {
                    it.setString(1, fodselsnummer)
                    it.executeQuery().list {
                        toBeskjed()
                    }
                }

fun Connection.getBeskjedById(id: Int): Beskjed =
        prepareStatement("""SELECT * FROM beskjed WHERE id = ?""")
                .use {
                    it.setInt(1, id)
                    it.executeQuery().singleResult() {
                        toBeskjed()
                    }
                }

fun Connection.getBeskjedByEventId(eventId: String): Beskjed =
        prepareStatement("""SELECT * FROM beskjed WHERE eventId = ?""")
                .use {
                    it.setString(1, eventId)
                    it.executeQuery().singleResult() {
                        toBeskjed()
                    }
                }

private fun ResultSet.toBeskjed(): Beskjed {
    return Beskjed(
            uid = getString("uid"),
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
            synligFremTil = getNullableLocalDateTime("synligFremTil"),
            aktiv = getBoolean("aktiv")
    )
}

private fun ResultSet.getNullableLocalDateTime(label: String): LocalDateTime? {
    return getTimestamp(label)?.toLocalDateTime()
}

