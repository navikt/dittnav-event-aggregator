package no.nav.personbruker.dittnav.eventaggregator.beskjed.archive

import no.nav.personbruker.dittnav.eventaggregator.archive.BrukernotifikasjonArchiveDTO
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.EPOCH_START
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.*
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusEnum.FERDIGSTILT
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import java.time.LocalDateTime
import java.time.ZoneId

private const val getBeskjedToArchiveQuery = """
    SELECT 
      beskjed.fodselsnummer,
      beskjed.eventId,
      beskjed.tekst,
      beskjed.link,
      beskjed.sikkerhetsnivaa,
      beskjed.aktiv,
      beskjed.appnavn,
      beskjed.forstBehandlet,
      dns.status as dns_status,
      dns.kanaler as dns_kanaler
    FROM
      beskjed
        LEFT JOIN doknotifikasjon_status_beskjed as dns ON beskjed.eventId = dns.eventId
    WHERE
      beskjed.forstBehandlet between ? and ?
    limit 1000
"""

private const val insertBeskjedArchiveQuery = """
    INSERT INTO beskjed_arkiv (fodselsnummer, eventid, tekst, link, sikkerhetsnivaa, aktiv, produsentApp, eksternVarslingSendt, eksternVarslingKanaler, forstbehandlet, arkivert)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
"""

private const val deleteDoknotifikasjonStatusBeskjedQuery = """
    DELETE FROM doknotifikasjon_status_beskjed WHERE eventId = ANY(?)
"""

private const val deleteBeskjedQuery = """
    DELETE FROM beskjed WHERE eventId = ANY(?)
"""

fun Connection.getBeskjedAsArchiveDtoOlderThan(dateThreshold: LocalDateTime): List<BrukernotifikasjonArchiveDTO> {
    return prepareStatement(getBeskjedToArchiveQuery)
        .use {
            it.setObject(1, EPOCH_START, Types.TIMESTAMP)
            it.setObject(2, dateThreshold, Types.TIMESTAMP)
            it.executeQuery().list {
                toBrukernotifikasjonArchiveDTO()
            }
        }
}

fun Connection.createBeskjedInArchive(toArchive: List<BrukernotifikasjonArchiveDTO>) {
    prepareStatement(insertBeskjedArchiveQuery).use { statement ->
        toArchive.forEach { beskjedToArchive ->
            statement.setParametersForSingleRow(beskjedToArchive)
            statement.addBatch()
        }
        statement.executeBatch()
    }
}

fun Connection.deleteDoknotifikasjonStatusBeskjedWithEventIds(eventIds: List<String>) {
    prepareStatement(deleteDoknotifikasjonStatusBeskjedQuery).use {
        it.setArray(1, toVarcharArray(eventIds))
        it.executeUpdate()
    }
}

fun Connection.deleteBeskjedWithEventIds(eventIds: List<String>) {
    prepareStatement(deleteBeskjedQuery).use {
        it.setArray(1, toVarcharArray(eventIds))
        it.executeUpdate()
    }
}

private fun PreparedStatement.setParametersForSingleRow(beskjedArchiveDTO: BrukernotifikasjonArchiveDTO) {
    setString(1, beskjedArchiveDTO.fodselsnummer)
    setString(2, beskjedArchiveDTO.eventId)
    setString(3, beskjedArchiveDTO.tekst)
    setString(4, beskjedArchiveDTO.link)
    setInt(5, beskjedArchiveDTO.sikkerhetsnivaa)
    setBoolean(6, beskjedArchiveDTO.aktiv)
    setString(7, beskjedArchiveDTO.produsentApp)
    setBoolean(8, beskjedArchiveDTO.eksternVarslingSendt)
    setString(9, beskjedArchiveDTO.eksternVarslingKanaler)
    setObject(10, beskjedArchiveDTO.forstBehandlet, Types.TIMESTAMP)
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
