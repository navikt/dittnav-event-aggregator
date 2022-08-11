package no.nav.personbruker.dittnav.eventaggregator.beskjed.archive

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
      b.fodselsnummer,
      b.eventId,
      b.tekst,
      b.link,
      b.sikkerhetsnivaa,
      b.aktiv,
      b.forstBehandlet,
      dns.status as dns_status,
      dns.kanaler as dns_kanaler
    FROM
      beskjed as b
        LEFT JOIN doknotifikasjon_status_beskjed as dns ON b.eventId = dns.eventId
    WHERE
      b.forstBehandlet < ?
    LIMIT 500
"""

private const val insertBeskjedArchiveQuery = """
    INSERT INTO beskjed_archive (fodselsnummer, eventid, tekst, link, sikkerhetsnivaa, aktiv, eksternVarslingSendt, eksternVarslingKanaler, forstbehandlet, arkivert)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ON CONFLICT DO NOTHING 
"""

private const val deleteDoknotifikasjonStatusBeskjedQuery = """
    DELETE FROM doknotifikasjon_status_beskjed WHERE eventId = ANY(?)
"""

private const val deleteBeskjedQuery = """
    DELETE FROM beskjed WHERE eventId = ANY(?)
"""

fun Connection.getBeskjedArchiveDtoOlderThan(dateThreshold: LocalDateTime): List<BeskjedArchiveDTO> {
    return prepareStatement(getBeskjedToArchiveQuery)
        .use {
            it.setObject(1, dateThreshold, Types.TIMESTAMP)
            it.executeQuery().list {
                toBeskjedArchiveDTO()
            }
        }
}

fun Connection.createBeskjedInArchive(toArchive: List<BeskjedArchiveDTO>) {
    prepareStatement(insertBeskjedArchiveQuery).use { statement ->
        toArchive.forEach { beskjedToArchive ->
            statement.setParametersForSingleRow(beskjedToArchive)
            statement.addBatch()
        }
        statement.executeQuery()
    }
}

fun Connection.deleteDoknotifikasjonStatusBeskjedWithEventIds(eventIds: List<String>) {
    prepareStatement(deleteDoknotifikasjonStatusBeskjedQuery).use {
        it.setArray(1, toVarcharArray(eventIds))
        it.executeQuery()
    }
}

fun Connection.deleteBeskjedWithEventIds(eventIds: List<String>) {
    prepareStatement(deleteBeskjedQuery).use {
        it.setArray(1, toVarcharArray(eventIds))
        it.executeQuery()
    }
}

private fun PreparedStatement.setParametersForSingleRow(beskjedArchiveDTO: BeskjedArchiveDTO) {
    setString(1, beskjedArchiveDTO.fodselsnummer)
    setString(2, beskjedArchiveDTO.eventId)
    setString(3, beskjedArchiveDTO.tekst)
    setString(4, beskjedArchiveDTO.link)
    setInt(5, beskjedArchiveDTO.sikkerhetsnivaa)
    setBoolean(6, beskjedArchiveDTO.aktiv)
    setBoolean(7, beskjedArchiveDTO.eksternVarslingSendt)
    setString(8, beskjedArchiveDTO.eksternVarslingKanaler)
    setObject(9, beskjedArchiveDTO.forstBehandlet, Types.TIMESTAMP)
    setObject(10, LocalDateTime.now(ZoneId.of("UTC")), Types.TIMESTAMP)
}

private fun ResultSet.toBeskjedArchiveDTO(): BeskjedArchiveDTO {
    return BeskjedArchiveDTO(
        fodselsnummer = getString("fodselsnummer"),
        eventId = getString("eventId"),
        tekst = getString("tekst"),
        link = getString("link"),
        sikkerhetsnivaa = getInt("sikkerhetsnivaa"),
        aktiv = getBoolean("aktiv"),
        forstBehandlet = getUtcDateTime("forstBehandlet"),
        eksternVarslingSendt = getEksternVarslingSendt(),
        eksternVarslingKanaler = getString("dns_kanaler")
    )
}

private fun ResultSet.getEksternVarslingSendt(): Boolean {
    return FERDIGSTILT.name == getString("dns_status")
}
