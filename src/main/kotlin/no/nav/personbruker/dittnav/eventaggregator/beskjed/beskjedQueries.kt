package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.*
import no.nav.personbruker.dittnav.eventaggregator.done.Done
import java.sql.*
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun Connection.getAllBeskjed(): List<Beskjed> =
        prepareStatement("""SELECT * FROM beskjed""")
                .use {
                    it.executeQuery().list {
                        toBeskjed()
                    }
                }

private val createQuery = """INSERT INTO beskjed (uid, systembruker, eventTidspunkt, fodselsnummer, eventId, grupperingsId, tekst, link, sikkerhetsnivaa, sistOppdatert, synligFremTil, aktiv, eksternVarsling, prefererteKanaler)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

fun Connection.createBeskjed(beskjed: Beskjed): PersistActionResult =
        executePersistQuery(createQuery) {
            buildStatementForSingleRow(beskjed)
        }

fun Connection.createBeskjeder(beskjeder: List<Beskjed>): ListPersistActionResult<Beskjed> =
        executeBatchPersistQuery(createQuery) {
            beskjeder.forEach { beskjed ->
                buildStatementForSingleRow(beskjed)
                addBatch()
            }
        }.toBatchPersistResult(beskjeder)

private fun PreparedStatement.buildStatementForSingleRow(beskjed: Beskjed) {
    setString(1, beskjed.uid)
    setString(2, beskjed.systembruker)
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
    setBoolean(13, beskjed.eksternVarsling)
    setObject(14, beskjed.prefererteKanaler.joinToString(","))
}

fun Connection.setBeskjederAktivflagg(doneEvents: List<Done>, aktiv: Boolean) {
    executeBatchUpdateQuery("""UPDATE beskjed SET aktiv = ? WHERE eventId = ? AND systembruker = ? AND fodselsnummer = ?""") {
        doneEvents.forEach { done ->
            setBoolean(1, aktiv)
            setString(2, done.eventId)
            setString(3, done.systembruker)
            setString(4, done.fodselsnummer)
            addBatch()
        }
    }
}

fun Connection.getExpiredBeskjedFromCursor(): List<Beskjed> {
    val now = LocalDateTime.now(ZoneId.of("UTC"))
    return prepareStatement("""SELECT * FROM beskjed WHERE aktiv = true AND synligFremTil <= ? LIMIT 10000""")
            .use {
                it.setObject(1, now, Types.TIMESTAMP)
                it.executeQuery().list { toBeskjed() }
            }
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
            systembruker = getString("systembruker"),
            eventTidspunkt = getUtcDateTime("eventTidspunkt"),
            fodselsnummer = getString("fodselsnummer"),
            eventId = getString("eventId"),
            grupperingsId = getString("grupperingsId"),
            tekst = getString("tekst"),
            link = getString("link"),
            sikkerhetsnivaa = getInt("sikkerhetsnivaa"),
            sistOppdatert = getUtcDateTime("sistOppdatert"),
            synligFremTil = getNullableLocalDateTime("synligFremTil"),
            aktiv = getBoolean("aktiv"),
            eksternVarsling = getBoolean("eksternVarsling"),
            prefererteKanaler = getListFromSeparatedString("prefererteKanaler", ",")
    )
}
