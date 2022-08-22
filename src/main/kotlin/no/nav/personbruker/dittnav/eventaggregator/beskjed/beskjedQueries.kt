package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.EPOCH_START
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.*
import no.nav.personbruker.dittnav.eventaggregator.done.Done
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

private val createQuery = """INSERT INTO beskjed (systembruker, eventTidspunkt, forstBehandlet, fodselsnummer, eventId, grupperingsId, tekst, link, sikkerhetsnivaa, sistOppdatert, synligFremTil, aktiv, eksternVarsling, prefererteKanaler, namespace, appnavn)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""



fun Connection.createBeskjed(beskjed: Beskjed): PersistActionResult =
        executePersistQuery(createQuery) {
            setParametersForSingleRow(beskjed)
        }

fun Connection.createBeskjeder(beskjeder: List<Beskjed>): ListPersistActionResult<Beskjed> =
        executeBatchPersistQueryIgnoreConflict(createQuery) {
            beskjeder.forEach { beskjed ->
                setParametersForSingleRow(beskjed)
                addBatch()
            }
        }.toBatchPersistResult(beskjeder)

fun Connection.getBeskjedWithEksternVarslingForEventIds(eventIds: List<String>): List<Beskjed> =
    prepareStatement("""SELECT * FROM beskjed WHERE eksternvarsling = true AND eventid = ANY(?)""")
        .use {
            it.setArray(1, toVarcharArray(eventIds))
            it.executeQuery().list {
                toBeskjed()
            }
        }

private fun PreparedStatement.setParametersForSingleRow(beskjed: Beskjed) {
    setString(1, beskjed.systembruker)
    setObject(2, beskjed.eventTidspunkt, Types.TIMESTAMP)
    setObject(3, beskjed.forstBehandlet, Types.TIMESTAMP)
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
    setString(15, beskjed.namespace)
    setString(16, beskjed.appnavn)
}

fun Connection.setBeskjederAktivflagg(doneEvents: List<Done>, aktiv: Boolean) {
    executeBatchUpdateQuery("""UPDATE beskjed SET aktiv = ? WHERE eventId = ? AND fodselsnummer = ?""") {
        doneEvents.forEach { done ->
            setBoolean(1, aktiv)
            setString(2, done.eventId)
            setString(3, done.fodselsnummer)
            addBatch()
        }
    }
}

fun Connection.getExpiredBeskjedFromCursor(): List<Beskjed> {
    return prepareStatement("""SELECT * FROM beskjed WHERE aktiv = true AND synligFremTil between ? and ? LIMIT 10000""")
            .use {
                it.setObject(1, EPOCH_START, Types.TIMESTAMP)
                it.setObject(2, nowAtUtc(), Types.TIMESTAMP)
                it.executeQuery().list { toBeskjed() }
            }
}

fun ResultSet.toBeskjed(): Beskjed {
    return Beskjed(
            id = getInt("id"),
            systembruker = getString("systembruker"),
            namespace = getString("namespace"),
            appnavn = getString("appnavn"),
            eventTidspunkt = getUtcDateTime("eventTidspunkt"),
            forstBehandlet = getUtcDateTime("forstBehandlet"),
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
