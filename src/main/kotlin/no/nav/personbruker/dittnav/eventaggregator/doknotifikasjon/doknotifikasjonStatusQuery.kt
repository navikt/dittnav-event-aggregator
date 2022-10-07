package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.*
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

private fun getQuery(eventType: String) = """
    SELECT * FROM doknotifikasjon_status_${eventType} WHERE eventId = ANY(?)
"""

private fun upsertQuery(eventType: String) = """
    INSERT INTO doknotifikasjon_status_${eventType}(eventId, status, melding, distribusjonsId, kanaler, tidspunkt, antall_oppdateringer) VALUES(?, ?, ?, ?, ?, ?, ?)
    ON CONFLICT (eventId) DO 
        UPDATE SET 
            status = excluded.status,
            melding = excluded.melding,
            distribusjonsId = excluded.distribusjonsId,
            kanaler = excluded.kanaler,
            tidspunkt = excluded.tidspunkt,
            antall_oppdateringer = excluded.antall_oppdateringer
"""

private val upsertQueryBeskjed = upsertQuery("beskjed")
private val upsertQueryOppgave = upsertQuery("oppgave")
private val upsertQueryInnboks = upsertQuery("innboks")

fun Connection.upsertDoknotifikasjonStatusForBeskjed(statuses: List<DoknotifikasjonStatusDto>) =
    executeBatchPersistQuery(upsertQueryBeskjed) {
        statuses.forEach { dokStatus ->
            buildStatementForSingleRow(dokStatus)
            addBatch()
        }
    }.toBatchPersistResult(statuses)

fun Connection.upsertDoknotifikasjonStatusForOppgave(statuses: List<DoknotifikasjonStatusDto>) =
    executeBatchPersistQuery(upsertQueryOppgave) {
        statuses.forEach { dokStatus ->
            buildStatementForSingleRow(dokStatus)
            addBatch()
        }
    }.toBatchPersistResult(statuses)

fun Connection.upsertDoknotifikasjonStatusForInnboks(statuses: List<DoknotifikasjonStatusDto>) =
    executeBatchPersistQuery(upsertQueryInnboks) {
        statuses.forEach { dokStatus ->
            buildStatementForSingleRow(dokStatus)
            addBatch()
        }
    }.toBatchPersistResult(statuses)

private fun PreparedStatement.buildStatementForSingleRow(dokStatus: DoknotifikasjonStatusDto) {
    setString(1, dokStatus.eventId)
    setString(2, dokStatus.status)
    setString(3, dokStatus.melding)
    setObject(4, dokStatus.distribusjonsId, Types.BIGINT)
    setString(5, dokStatus.kanaler.joinToString(","))
    setObject(6, nowAtUtc(), Types.TIMESTAMP)
    setInt(7, dokStatus.antallOppdateringer)
}

private fun ResultSet.toDoknotifikasjonStatusDto() =
    DoknotifikasjonStatusDto(
        eventId = getString("eventId"),
        status = getString("status"),
        melding = getString("melding"),
        distribusjonsId = getLong("distribusjonsId"),
        kanaler = getListFromSeparatedString("kanaler", ","),
        antallOppdateringer = getInt("antall_oppdateringer"),
        bestillerAppnavn = ""
    )
