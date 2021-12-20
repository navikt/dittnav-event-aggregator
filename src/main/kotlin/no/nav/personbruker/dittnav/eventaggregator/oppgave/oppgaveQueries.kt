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

fun Connection.getAllOppgave(): List<Oppgave> =
        prepareStatement("""SELECT * FROM oppgave""")
                .use {
                    it.executeQuery().list {
                        toOppgave()
                    }
                }

private val createQuery = """INSERT INTO oppgave (systembruker, eventTidspunkt, fodselsnummer, eventId, grupperingsId, tekst, link, sikkerhetsnivaa, sistOppdatert, aktiv, eksternVarsling, prefererteKanaler, namespace, appnavn, synligFremTil) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,?, ?, ?, ?)"""

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
    setString(3, oppgave.fodselsnummer)
    setString(4, oppgave.eventId)
    setString(5, oppgave.grupperingsId)
    setString(6, oppgave.tekst)
    setString(7, oppgave.link)
    setInt(8, oppgave.sikkerhetsnivaa)
    setObject(9, oppgave.sistOppdatert, Types.TIMESTAMP)
    setBoolean(10, oppgave.aktiv)
    setBoolean(11, oppgave.eksternVarsling)
    setObject(12, oppgave.prefererteKanaler.joinToString(","))
    setString(13, oppgave.namespace)
    setString(14, oppgave.appnavn)
    setObject(15, oppgave.synligFremTil, Types.TIMESTAMP)
}

fun Connection.setOppgaverAktivFlag(doneEvents: List<Done>, aktiv: Boolean) {
    executeBatchUpdateQuery("""UPDATE oppgave SET aktiv = ? WHERE eventId = ? AND systembruker = ? AND fodselsnummer = ?""") {
        doneEvents.forEach { done ->
            setBoolean(1, aktiv)
            setString(2, done.eventId)
            setString(3, done.systembruker)
            setString(4, done.fodselsnummer)
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


fun Connection.getAllOppgaveByAktiv(aktiv: Boolean): List<Oppgave> =
        prepareStatement("""SELECT * FROM oppgave WHERE aktiv = ?""")
                .use {
                    it.setBoolean(1, aktiv)
                    it.executeQuery().list {
                        toOppgave()
                    }
                }

fun Connection.getOppgaveByFodselsnummer(fodselsnummer: String): List<Oppgave> =
        prepareStatement("""SELECT * FROM oppgave WHERE fodselsnummer = ?""")
                .use {
                    it.setString(1, fodselsnummer)
                    it.executeQuery().list {
                        toOppgave()
                    }
                }

fun Connection.getOppgaveById(id: Int): Oppgave =
        prepareStatement("""SELECT * FROM oppgave WHERE id = ?""")
                .use {
                    it.setInt(1, id)
                    it.executeQuery().singleResult {
                        toOppgave()
                    }
                }

fun Connection.getOppgaveByEventId(eventId: String): Oppgave =
        prepareStatement("""SELECT * FROM oppgave WHERE eventId = ?""")
                .use {
                    it.setString(1, eventId)
                    it.executeQuery().singleResult {
                        toOppgave()
                    }
                }

private fun ResultSet.toOppgave(): Oppgave {
    return Oppgave(
            id = getInt("id"),
            systembruker = getString("systembruker"),
            namespace = getString("namespace"),
            appnavn = getString("appnavn"),
            eventTidspunkt = getUtcDateTime("eventTidspunkt"),
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
