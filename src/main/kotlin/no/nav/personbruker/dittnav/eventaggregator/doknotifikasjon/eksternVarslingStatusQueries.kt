package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper
import no.nav.personbruker.dittnav.eventaggregator.common.database.getListFromString
import no.nav.personbruker.dittnav.eventaggregator.common.database.getUtcDateTime
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import org.postgresql.util.PGobject
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

private fun getQuery(eventType: String) = """
    SELECT * FROM ekstern_varsling_status_${eventType} WHERE eventId = ?
"""

private fun upsertQuery(eventType: String) = """
    INSERT INTO ekstern_varsling_status_${eventType}(eventId, kanaler, eksternVarslingSendt, renotifikasjonSendt, sistMottattStatus, historikk, sistOppdatert) VALUES(?, ?, ?, ?, ?, ?, ?)
    ON CONFLICT (eventId) DO 
        UPDATE SET 
            kanaler = excluded.kanaler,
            eksternVarslingSendt = excluded.eksternVarslingSendt,
            renotifikasjonSendt = excluded.renotifikasjonSendt,
            sistMottattStatus = excluded.sistMottattStatus,
            historikk = excluded.historikk,
            sistOppdatert = excluded.sistOppdatert
"""

private val getQueryBeskjed = getQuery("beskjed")
private val getQueryOppgave = getQuery("oppgave")
private val getQueryInnboks = getQuery("innboks")

private val upsertQueryBeskjed = upsertQuery("beskjed")
private val upsertQueryOppgave = upsertQuery("oppgave")
private val upsertQueryInnboks = upsertQuery("innboks")

private val objectMapper = jacksonMapperBuilder()
    .addModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .build()
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)

fun Connection.getEksternVarslingStatusIfExists(eventId: String, varselType: VarselType): EksternVarslingStatus? {
    return when(varselType) {
        VarselType.BESKJED -> getStatusIfExists(eventId, getQueryBeskjed)
        VarselType.OPPGAVE -> getStatusIfExists(eventId, getQueryOppgave)
        VarselType.INNBOKS -> getStatusIfExists(eventId, getQueryInnboks)
    }
}

fun Connection.upsertEksternVarslingStatus(status: EksternVarslingStatus, varselType: VarselType) =
    when(varselType) {
        VarselType.BESKJED -> upsertEksternVarslingStatus(upsertQueryBeskjed) { buildStatement(status) }
        VarselType.OPPGAVE -> upsertEksternVarslingStatus(upsertQueryOppgave) { buildStatement(status) }
        VarselType.INNBOKS -> upsertEksternVarslingStatus(upsertQueryInnboks) { buildStatement(status) }
    }

private fun Connection.getStatusIfExists(eventId: String, query: String): EksternVarslingStatus? =
    prepareStatement(query)
        .use {
            it.setString(1, eventId)
            it.executeQuery().use {
                    resultSet -> if(resultSet.next()) resultSet.toEksternVarslingStatus() else null
            }
        }


private fun Connection.upsertEksternVarslingStatus(query: String, paramInit: PreparedStatement.() -> Unit) {
    return prepareStatement(query)
        .use {
            it.paramInit()
            it.executeUpdate()
        }
}

private fun PreparedStatement.buildStatement(status: EksternVarslingStatus) {
    val historikkBlob = PGobject().apply {
        type = "json"
        value = objectMapper.writeValueAsString(status.historikk)
    }

    setString(1, status.eventId)
    setString(2, status.kanaler.joinToString(","))
    setBoolean(3, status.eksternVarslingSendt)
    setBoolean(4, status.renotifikasjonSendt)
    setString(5, status.sistMottattStatus)
    setObject(6, historikkBlob)
    setObject(7, LocalDateTimeHelper.nowAtUtc(), Types.TIMESTAMP)
}

private fun ResultSet.toEksternVarslingStatus(): EksternVarslingStatus {
    val historikkJson = getString("historikk")

    val historikk: List<EksternVarslingHistorikkEntry> = if (historikkJson == null) {
        emptyList()
    } else {
        objectMapper.readValue(historikkJson)
    }

    return EksternVarslingStatus(
        eventId = getString("eventId"),
        eksternVarslingSendt = getBoolean("eksternVarslingSendt"),
        renotifikasjonSendt = getBoolean("renotifikasjonSendt"),
        kanaler = getListFromString("kanaler"),
        sistOppdatert = getUtcDateTime("sistOppdatert"),
        sistMottattStatus = getString("sistMottattStatus"),
        historikk = historikk
    )
}
