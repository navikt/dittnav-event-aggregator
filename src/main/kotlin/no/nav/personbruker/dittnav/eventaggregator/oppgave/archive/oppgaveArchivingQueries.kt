package no.nav.personbruker.dittnav.eventaggregator.oppgave.archive

import no.nav.personbruker.dittnav.eventaggregator.archive.BrukernotifikasjonArchiveDTO
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.*
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusEnum.FERDIGSTILT
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import java.time.LocalDateTime
import java.time.ZoneId

private const val getOppgaveToArchiveQuery = """
    SELECT 
      oppgave.fodselsnummer,
      oppgave.eventId,
      oppgave.tekst,
      oppgave.link,
      oppgave.sikkerhetsnivaa,
      oppgave.aktiv,
      oppgave.appnavn,
      oppgave.forstBehandlet,
      dns.status as dns_status,
      dns.kanaler as dns_kanaler
    FROM
      oppgave
        LEFT JOIN doknotifikasjon_status_oppgave as dns ON oppgave.eventId = dns.eventId
    WHERE
      oppgave.forstBehandlet < ?
    LIMIT 500
"""

private const val insertOppgaveArchiveQuery = """
    INSERT INTO oppgave_arkiv (fodselsnummer, eventid, tekst, link, sikkerhetsnivaa, aktiv, produsentApp, eksternVarslingSendt, eksternVarslingKanaler, forstbehandlet, arkivert)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
"""

private const val deleteDoknotifikasjonStatusOppgaveQuery = """
    DELETE FROM doknotifikasjon_status_oppgave WHERE eventId = ANY(?)
"""

private const val deleteOppgaveQuery = """
    DELETE FROM oppgave WHERE eventId = ANY(?)
"""

fun Connection.getOppgaveAsArchiveDtoOlderThan(dateThreshold: LocalDateTime): List<BrukernotifikasjonArchiveDTO> {
    return prepareStatement(getOppgaveToArchiveQuery)
        .use {
            it.setObject(1, dateThreshold, Types.TIMESTAMP)
            it.executeQuery().list {
                toBrukernotifikasjonArchiveDTO()
            }
        }
}

fun Connection.createOppgaveInArchive(toArchive: List<BrukernotifikasjonArchiveDTO>) {
    prepareStatement(insertOppgaveArchiveQuery).use { statement ->
        toArchive.forEach { oppgaveToArchive ->
            statement.setParametersForSingleRow(oppgaveToArchive)
            statement.addBatch()
        }
        statement.executeBatch()
    }
}

fun Connection.deleteDoknotifikasjonStatusOppgaveWithEventIds(eventIds: List<String>) {
    prepareStatement(deleteDoknotifikasjonStatusOppgaveQuery).use {
        it.setArray(1, toVarcharArray(eventIds))
        it.executeUpdate()
    }
}

fun Connection.deleteOppgaveWithEventIds(eventIds: List<String>) {
    prepareStatement(deleteOppgaveQuery).use {
        it.setArray(1, toVarcharArray(eventIds))
        it.executeUpdate()
    }
}

private fun PreparedStatement.setParametersForSingleRow(oppgaveArchiveDTO: BrukernotifikasjonArchiveDTO) {
    setString(1, oppgaveArchiveDTO.fodselsnummer)
    setString(2, oppgaveArchiveDTO.eventId)
    setString(3, oppgaveArchiveDTO.tekst)
    setString(4, oppgaveArchiveDTO.link)
    setInt(5, oppgaveArchiveDTO.sikkerhetsnivaa)
    setBoolean(6, oppgaveArchiveDTO.aktiv)
    setString(7, oppgaveArchiveDTO.produsentApp)
    setBoolean(8, oppgaveArchiveDTO.eksternVarslingSendt)
    setString(9, oppgaveArchiveDTO.eksternVarslingKanaler)
    setObject(10, oppgaveArchiveDTO.forstBehandlet, Types.TIMESTAMP)
    setObject(11, nowAtUtc(), Types.TIMESTAMP)
}

private fun ResultSet.toBrukernotifikasjonArchiveDTO(): BrukernotifikasjonArchiveDTO {
    return BrukernotifikasjonArchiveDTO(
        fodselsnummer = getString("fodselsnummer"),
        eventId = getString("eventId"),
        tekst = getString("tekst"),
        link = getString("link"),
        sikkerhetsnivaa = getInt("sikkerhetsnivaa"),
        aktiv = getBoolean("aktiv"),
        produsentApp = getString("appnavn"),
        forstBehandlet = getUtcDateTime("forstBehandlet"),
        eksternVarslingSendt = getEksternVarslingSendt(),
        eksternVarslingKanaler = getEksternVarslingKanaler()
    )
}

private fun ResultSet.getEksternVarslingSendt(): Boolean {
    return FERDIGSTILT.name == getString("dns_status")
}

private fun ResultSet.getEksternVarslingKanaler(): String {
    return getString("dns_kanaler") ?: ""
}
