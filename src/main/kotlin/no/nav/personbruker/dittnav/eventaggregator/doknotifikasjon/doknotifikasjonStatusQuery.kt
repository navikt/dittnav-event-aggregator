package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import no.nav.doknotifikasjon.schemas.DoknotifikasjonStatus
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.executeBatchPersistQuery
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.toBatchPersistResult
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Types
import java.time.LocalDateTime
import java.time.ZoneId

private const val upsertQueryBeskjed = """
    INSERT INTO doknotifikasjon_status_beskjed(eventId, status, melding, distribusjonsId, tidspunkt, antall_oppdateringer) VALUES(?, ?, ?, ?, ?, 1)
    ON CONFLICT (eventId) DO 
        UPDATE SET 
            status = excluded.status,
            melding = excluded.melding,
            distribusjonsId = excluded.distribusjonsId,
            tidspunkt = excluded.tidspunkt,
            antall_oppdateringer = doknotifikasjon_status_beskjed.antall_oppdateringer + 1
        WHERE excluded.status != doknotifikasjon_status_beskjed.status 
           OR excluded.melding != doknotifikasjon_status_beskjed.melding
           OR excluded.distribusjonsId != doknotifikasjon_status_beskjed.distribusjonsId
"""

private const val upsertQueryOppgave = """
    INSERT INTO doknotifikasjon_status_oppgave(eventId, status, melding, distribusjonsId, tidspunkt, antall_oppdateringer) VALUES(?, ?, ?, ?, ?, 1)
    ON CONFLICT (eventId) DO 
        UPDATE SET 
            status = excluded.status,
            melding = excluded.melding,
            distribusjonsId = excluded.distribusjonsId,
            tidspunkt = excluded.tidspunkt,
            antall_oppdateringer = doknotifikasjon_status_oppgave.antall_oppdateringer + 1
        WHERE excluded.status != doknotifikasjon_status_oppgave.status 
           OR excluded.melding != doknotifikasjon_status_oppgave.melding
           OR excluded.distribusjonsId != doknotifikasjon_status_oppgave.distribusjonsId
"""

private const val upsertQueryInnboks = """
    INSERT INTO doknotifikasjon_status_innboks(eventId, status, melding, distribusjonsId, tidspunkt, antall_oppdateringer) VALUES(?, ?, ?, ?, ?, 1)
    ON CONFLICT (eventId) DO 
        UPDATE SET 
            status = excluded.status,
            melding = excluded.melding,
            distribusjonsId = excluded.distribusjonsId,
            tidspunkt = excluded.tidspunkt,
            antall_oppdateringer = doknotifikasjon_status_innboks.antall_oppdateringer + 1
        WHERE excluded.status != doknotifikasjon_status_innboks.status 
           OR excluded.melding != doknotifikasjon_status_innboks.melding
           OR excluded.distribusjonsId != doknotifikasjon_status_innboks.distribusjonsId
"""

fun Connection.upsertDoknotifikasjonStatusForBeskjed(statuses: List<DoknotifikasjonStatus>) =
    executeBatchPersistQuery(upsertQueryBeskjed) {
        statuses.forEach { dokStatus ->
            buildStatementForSingleRow(dokStatus)
            addBatch()
        }
    }.toBatchPersistResult(statuses)

fun Connection.upsertDoknotifikasjonStatusForOppgave(statuses: List<DoknotifikasjonStatus>) =
    executeBatchPersistQuery(upsertQueryOppgave) {
        statuses.forEach { dokStatus ->
            buildStatementForSingleRow(dokStatus)
            addBatch()
        }
    }.toBatchPersistResult(statuses)

fun Connection.upsertDoknotifikasjonStatusForInnboks(statuses: List<DoknotifikasjonStatus>) =
    executeBatchPersistQuery(upsertQueryInnboks) {
        statuses.forEach { dokStatus ->
            buildStatementForSingleRow(dokStatus)
            addBatch()
        }
    }.toBatchPersistResult(statuses)

private fun PreparedStatement.buildStatementForSingleRow(dokStatus: DoknotifikasjonStatus) {
    setString(1, dokStatus.getBestillingsId())
    setString(2, dokStatus.getStatus())
    setString(3, dokStatus.getMelding())
    setObject(4, dokStatus.getDistribusjonId(), Types.BIGINT)
    setObject(5, LocalDateTime.now(ZoneId.of("UTC")), Types.TIMESTAMP)
}
