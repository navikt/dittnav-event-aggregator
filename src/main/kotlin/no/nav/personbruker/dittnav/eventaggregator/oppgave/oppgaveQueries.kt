package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.*
import no.nav.personbruker.dittnav.eventaggregator.done.Done
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import java.time.LocalDateTime
import java.time.ZoneId

private val createQuery = """INSERT INTO oppgave (systembruker, eventTidspunkt, forstBehandlet, fodselsnummer, eventId, grupperingsId, tekst, link, sikkerhetsnivaa, sistOppdatert, aktiv, eksternVarsling, prefererteKanaler, namespace, appnavn, synligFremTil) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,?, ?, ?, ?)"""

fun Connection.createOppgaver(oppgaver: List<Oppgave>) =
        executeBatchPersistQuery(createQuery) {
            oppgaver.forEach { oppgave ->
                buildStatementForSingleRow(oppgave)
                addBatch()
            }
        }.toBatchPersistResult(oppgaver)

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
    executeBatchUpdateQuery("""UPDATE oppgave SET aktiv = ? WHERE eventId = ? AND fodselsnummer = ?""") {
        doneEvents.forEach { done ->
            setBoolean(1, aktiv)
            setString(2, done.eventId)
            setString(3, done.fodselsnummer)
            addBatch()
        }
    }
}

fun Connection.getExpiredOppgave(): List<Oppgave> {
    val now = LocalDateTime.now(ZoneId.of("UTC"))
    return prepareStatement("""SELECT * FROM oppgave WHERE aktiv = true AND synligFremTil <= ? LIMIT 10000""")
        .use {
            it.setObject(1, now, Types.TIMESTAMP)
            it.executeQuery().list { toOppgave() }
        }
}

fun ResultSet.toOppgave(): Oppgave {
    return Oppgave(
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
            aktiv = getBoolean("aktiv"),
            eksternVarsling = getBoolean("eksternVarsling"),
            prefererteKanaler = getListFromSeparatedString("prefererteKanaler", ","),
            synligFremTil = getNullableLocalDateTime("synligFremTil")
    )
}
