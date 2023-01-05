package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.executeBatchUpdateQuery
import no.nav.personbruker.dittnav.eventaggregator.common.database.executePersistQuery
import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import no.nav.personbruker.dittnav.eventaggregator.done.Done
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Types

private const val createQuery = """INSERT INTO oppgave (systembruker, eventTidspunkt, forstBehandlet, fodselsnummer, eventId, grupperingsId, tekst, link, sikkerhetsnivaa, sistOppdatert, aktiv, eksternVarsling, prefererteKanaler, namespace, appnavn, synligFremTil) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,?, ?, ?, ?)"""

fun Connection.createOppgave(oppgave: Oppgave): PersistActionResult =
        executePersistQuery(createQuery) {
            buildStatementForSingleRow(oppgave)
            addBatch()
        }

private fun PreparedStatement.buildStatementForSingleRow(oppgave: Oppgave) {
    setString(1, oppgave.systembruker)
    setObject(2, oppgave.eventTidspunkt, Types.TIMESTAMP)
    setObject(3, oppgave.forstBehandlet, Types.TIMESTAMP)
    setString(4, oppgave.fodselsnummer)
    setString(5, oppgave.eventId)
    setString(6, oppgave.grupperingsId)
    setString(7, oppgave.tekst)
    setString(8, oppgave.link)
    setInt(9, oppgave.sikkerhetsnivaa)
    setObject(10, oppgave.sistOppdatert, Types.TIMESTAMP)
    setBoolean(11, oppgave.aktiv)
    setBoolean(12, oppgave.eksternVarsling)
    setObject(13, oppgave.prefererteKanaler.joinToString(","))
    setString(14, oppgave.namespace)
    setString(15, oppgave.appnavn)
    setObject(16, oppgave.synligFremTil, Types.TIMESTAMP)
}

fun Connection.setOppgaverAktivFlag(doneEvents: List<Done>, aktiv: Boolean) {
    executeBatchUpdateQuery("""UPDATE oppgave SET aktiv = ?, sistoppdatert = ? WHERE eventId = ?""") {
        doneEvents.forEach { done ->
            setBoolean(1, aktiv)
            setObject(2, nowAtUtc(), Types.TIMESTAMP)
            setString(3, done.eventId)
            addBatch()
        }
    }
}

fun Connection.setExpiredOppgaveAsInactive(): List<String> {
    return prepareStatement("""UPDATE oppgave set aktiv = false, sistoppdatert = ?, frist_utløpt = TRUE WHERE aktiv = true AND synligFremTil < ? RETURNING eventId""")
        .use {
            it.setObject(1, nowAtUtc(), Types.TIMESTAMP)
            it.setObject(2, nowAtUtc(), Types.TIMESTAMP)
            it.executeQuery().list {
                getString("eventId")
            }
        }
}
