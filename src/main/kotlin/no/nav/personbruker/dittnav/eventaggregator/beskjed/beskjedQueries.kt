package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.executeBatchUpdateQuery
import no.nav.personbruker.dittnav.eventaggregator.common.database.executePersistQuery
import no.nav.personbruker.dittnav.eventaggregator.done.Done
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Types

private const val createQuery =
    """INSERT INTO beskjed (systembruker, eventTidspunkt, forstBehandlet, fodselsnummer, eventId, grupperingsId, tekst, link, sikkerhetsnivaa, sistOppdatert, synligFremTil, aktiv, eksternVarsling, prefererteKanaler, namespace, appnavn)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""


fun Connection.createBeskjed(beskjed: Beskjed): PersistActionResult =
    executePersistQuery(createQuery) {
        setParametersForSingleRow(beskjed)
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
    executeBatchUpdateQuery("""UPDATE beskjed SET aktiv = ?, sistoppdatert = ? WHERE eventId = ?""") {
        doneEvents.forEach { done ->
            setBoolean(1, aktiv)
            setObject(2, nowAtUtc(), Types.TIMESTAMP)
            setString(3, done.eventId)
            addBatch()
        }
    }
}

fun Connection.setBeskjedInaktiv(eventId: String, fnr: String): Int {
    requireBeskjedExists(eventId, fnr)
    return prepareStatement("""UPDATE beskjed SET aktiv = FALSE, sistoppdatert = ? WHERE eventId = ? AND aktiv=TRUE""".trimMargin())
        .use {
            it.setObject(1, nowAtUtc(), Types.TIMESTAMP)
            it.setString(2, eventId)
            it.executeUpdate()
        }
}

private fun Connection.requireBeskjedExists(eventId: String, fnr: String) {
    prepareStatement("""SELECT * FROM beskjed WHERE eventId=?""".trimMargin())
        .use {
            it.setString(1, eventId)
            it.executeQuery().apply {
                if (!next()) {
                    throw BeskjedNotFoundException(eventId)
                }
                if (getString("fodselsnummer") != fnr) {
                    throw BeskjedDoesNotBelongToUserException(eventId)
                }
            }
        }
}

fun Connection.setExpiredBeskjedAsInactive(): Int {
    return prepareStatement("""UPDATE beskjed SET aktiv = FALSE, sistoppdatert = ? WHERE aktiv = TRUE AND synligFremTil < ?""")
        .use {
            it.setObject(1, nowAtUtc(), Types.TIMESTAMP)
            it.setObject(2, nowAtUtc(), Types.TIMESTAMP)
            it.executeUpdate()
        }
}

class BeskjedNotFoundException(eventId: String) :
    IllegalArgumentException("beskjed med eventId $eventId ikke funnet")

class BeskjedDoesNotBelongToUserException(val eventId: String) : IllegalArgumentException()