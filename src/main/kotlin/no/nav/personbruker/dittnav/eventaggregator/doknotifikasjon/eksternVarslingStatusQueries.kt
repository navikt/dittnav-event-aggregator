package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper
import no.nav.personbruker.dittnav.eventaggregator.common.database.getListFromSeparatedString
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

private fun getQuery(eventType: String) = """
    SELECT * FROM doknotifikasjon_status_${eventType} WHERE eventId = ?
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

private val getQueryBeskjed = getQuery("beskjed")
private val getQueryOppgave = getQuery("oppgave")
private val getQueryInnboks = getQuery("innboks")

private val upsertQueryBeskjed = upsertQuery("beskjed")
private val upsertQueryOppgave = upsertQuery("oppgave")
private val upsertQueryInnboks = upsertQuery("innboks")

fun Connection.getStatusIfExists(eventId: String, varselType: VarselType): DoknotifikasjonStatusDto? {
    return when(varselType) {
        VarselType.BESKJED -> getStatusIfExists(eventId, getQueryBeskjed)
        VarselType.OPPGAVE -> getStatusIfExists(eventId, getQueryOppgave)
        VarselType.INNBOKS -> getStatusIfExists(eventId, getQueryInnboks)
    }
}

fun Connection.upsertDoknotifikasjonStatus(status: DoknotifikasjonStatusDto, varselType: VarselType) =
    when(varselType) {
        VarselType.BESKJED -> upsertDoknotifikasjonStatus(upsertQueryBeskjed) { buildStatementForSingleRow(status) }
        VarselType.OPPGAVE -> upsertDoknotifikasjonStatus(upsertQueryOppgave) { buildStatementForSingleRow(status) }
        VarselType.INNBOKS -> upsertDoknotifikasjonStatus(upsertQueryInnboks) { buildStatementForSingleRow(status) }
    }

private fun Connection.getStatusIfExists(eventId: String, query: String): DoknotifikasjonStatusDto? =
    prepareStatement(query)
        .use {
            it.setString(1, eventId)
            it.executeQuery().use {
                    resultSet -> if(resultSet.next()) resultSet.toDoknotifikasjonStatusDto() else null
            }
        }

private fun Connection.upsertDoknotifikasjonStatus(query: String, paramInit: PreparedStatement.() -> Unit) {
    return prepareStatement(query)
        .use {
            it.paramInit()
            it.executeUpdate()
        }
}

private fun PreparedStatement.buildStatementForSingleRow(dokStatus: DoknotifikasjonStatusDto) {
    setString(1, dokStatus.eventId)
    setString(2, dokStatus.status)
    setString(3, dokStatus.melding)
    setObject(4, dokStatus.distribusjonsId, Types.BIGINT)
    setString(5, dokStatus.kanaler.joinToString(","))
    setObject(6, LocalDateTimeHelper.nowAtUtc(), Types.TIMESTAMP)
    setInt(7, dokStatus.antallOppdateringer)
}

private fun ResultSet.toDoknotifikasjonStatusDto(): DoknotifikasjonStatusDto {
    return DoknotifikasjonStatusDto(
            eventId = getString("eventId"),
            status = getString("status"),
            melding = getString("melding"),
            distribusjonsId = getLong("distribusjonsId"),
            kanaler = getListFromSeparatedString("kanaler", ","),
            antallOppdateringer = getInt("antall_oppdateringer"),
            bestillerAppnavn = ""
        )
}