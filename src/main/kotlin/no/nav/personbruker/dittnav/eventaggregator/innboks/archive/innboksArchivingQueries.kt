package no.nav.personbruker.dittnav.eventaggregator.innboks.archive

import no.nav.personbruker.dittnav.eventaggregator.archive.BrukernotifikasjonArchiveDTO
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.*
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusEnum.FERDIGSTILT
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import java.time.LocalDateTime
import java.time.ZoneId

private const val getInnboksToArchiveQuery = """
    SELECT 
      innboks.fodselsnummer,
      innboks.eventId,
      innboks.tekst,
      innboks.link,
      innboks.sikkerhetsnivaa,
      innboks.aktiv,
      innboks.appnavn,
      innboks.forstBehandlet,
      dns.status as dns_status,
      dns.kanaler as dns_kanaler
    FROM
      innboks
        LEFT JOIN doknotifikasjon_status_innboks as dns ON innboks.eventId = dns.eventId
    WHERE
      innboks.forstBehandlet < ?
    LIMIT 500
"""

private const val insertInnboksArchiveQuery = """
    INSERT INTO innboks_arkiv (fodselsnummer, eventid, tekst, link, sikkerhetsnivaa, aktiv, produsentApp, eksternVarslingSendt, eksternVarslingKanaler, forstbehandlet, arkivert)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
"""

private const val deleteDoknotifikasjonStatusInnboksQuery = """
    DELETE FROM doknotifikasjon_status_innboks WHERE eventId = ANY(?)
"""

private const val deleteInnboksQuery = """
    DELETE FROM innboks WHERE eventId = ANY(?)
"""

fun Connection.getInnboksAsArchiveDtoOlderThan(dateThreshold: LocalDateTime): List<BrukernotifikasjonArchiveDTO> {
    return prepareStatement(getInnboksToArchiveQuery)
        .use {
            it.setObject(1, dateThreshold, Types.TIMESTAMP)
            it.executeQuery().list {
                toBrukernotifikasjonArchiveDTO()
            }
        }
}

fun Connection.createInnboksInArchive(toArchive: List<BrukernotifikasjonArchiveDTO>) {
    prepareStatement(insertInnboksArchiveQuery).use { statement ->
        toArchive.forEach { innboksToArchive ->
            statement.setParametersForSingleRow(innboksToArchive)
            statement.addBatch()
        }
        statement.executeBatch()
    }
}

fun Connection.deleteDoknotifikasjonStatusInnboksWithEventIds(eventIds: List<String>) {
    prepareStatement(deleteDoknotifikasjonStatusInnboksQuery).use {
        it.setArray(1, toVarcharArray(eventIds))
        it.executeUpdate()
    }
}

fun Connection.deleteInnboksWithEventIds(eventIds: List<String>) {
    prepareStatement(deleteInnboksQuery).use {
        it.setArray(1, toVarcharArray(eventIds))
        it.executeUpdate()
    }
}

private fun PreparedStatement.setParametersForSingleRow(innboksArchiveDTO: BrukernotifikasjonArchiveDTO) {
    setString(1, innboksArchiveDTO.fodselsnummer)
    setString(2, innboksArchiveDTO.eventId)
    setString(3, innboksArchiveDTO.tekst)
    setString(4, innboksArchiveDTO.link)
    setInt(5, innboksArchiveDTO.sikkerhetsnivaa)
    setBoolean(6, innboksArchiveDTO.aktiv)
    setString(7, innboksArchiveDTO.produsentApp)
    setBoolean(8, innboksArchiveDTO.eksternVarslingSendt)
    setString(9, innboksArchiveDTO.eksternVarslingKanaler)
    setObject(10, innboksArchiveDTO.forstBehandlet, Types.TIMESTAMP)
    setObject(11, LocalDateTime.now(ZoneId.of("UTC")), Types.TIMESTAMP)
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