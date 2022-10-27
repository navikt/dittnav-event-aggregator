package no.nav.personbruker.dittnav.eventaggregator.archive

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.EPOCH_START
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.common.database.getUtcDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import no.nav.personbruker.dittnav.eventaggregator.common.database.toVarcharArray
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusEnum.FERDIGSTILT
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import java.time.LocalDateTime

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
      oppgave.forstBehandlet between ? and ?
    limit 1000
"""

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
      innboks.forstBehandlet between ? and ?
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

fun Connection.getArchivableBeskjeder(dateThreshold: LocalDateTime) = getVarselAsArchiveDtoOlderThan(dateThreshold, getBeskjedToArchiveQuery)
fun Connection.getArchivableOppgaver(dateThreshold: LocalDateTime) = getVarselAsArchiveDtoOlderThan(dateThreshold, getOppgaveToArchiveQuery)
fun Connection.getArchivableInnbokser(dateThreshold: LocalDateTime) = getVarselAsArchiveDtoOlderThan(dateThreshold, getInnboksToArchiveQuery)

private fun Connection.getVarselAsArchiveDtoOlderThan(dateThreshold: LocalDateTime, getArchivableVarselQuery: String): List<BrukernotifikasjonArchiveDTO> {
    return prepareStatement(getArchivableVarselQuery)
        .use {
            it.setObject(1, EPOCH_START, Types.TIMESTAMP)
            it.setObject(2, dateThreshold, Types.TIMESTAMP)
            it.executeQuery().list {
                toBrukernotifikasjonArchiveDTO()
            }
        }
}

fun Connection.createArchivedBeskjeder(toArchive: List<BrukernotifikasjonArchiveDTO>) {
    createVarselInArchive(toArchive, insertBeskjedArchiveQuery)
}
fun Connection.createArchivedOppgaver(toArchive: List<BrukernotifikasjonArchiveDTO>) {
    createVarselInArchive(toArchive, insertOppgaveArchiveQuery)
}
fun Connection.createArchivedInnbokser(toArchive: List<BrukernotifikasjonArchiveDTO>) {
    createVarselInArchive(toArchive, insertInnboksArchiveQuery)
}
private fun Connection.createVarselInArchive(toArchive: List<BrukernotifikasjonArchiveDTO>, insertVarselArchiveQuery: String) {
    prepareStatement(insertVarselArchiveQuery).use { statement ->
        toArchive.forEach { beskjedToArchive ->
            statement.setParametersForSingleRow(beskjedToArchive)
            statement.addBatch()
        }
        statement.executeBatch()
    }
}


fun Connection.deleteDoknotifikasjonStatusVarselBeskjed(eventIds: List<String>) {
    deleteDoknotifikasjonStatusVarsel(eventIds, deleteDoknotifikasjonStatusBeskjedQuery)
}
fun Connection.deleteDoknotifikasjonStatusVarselOppgave(eventIds: List<String>) {
    deleteDoknotifikasjonStatusVarsel(eventIds, deleteDoknotifikasjonStatusOppgaveQuery)
}
fun Connection.deleteDoknotifikasjonStatusVarselInnboks(eventIds: List<String>) {
    deleteDoknotifikasjonStatusVarsel(eventIds, deleteDoknotifikasjonStatusInnboksQuery)
}
private fun Connection.deleteDoknotifikasjonStatusVarsel(eventIds: List<String>, deleteDoknotifikasjonStatusQuery: String) {
    prepareStatement(deleteDoknotifikasjonStatusQuery).use {
        it.setArray(1, toVarcharArray(eventIds))
        it.executeUpdate()
    }
}

fun Connection.deleteBeskjeder(eventIds: List<String>) {
    deleteVarsler(eventIds, deleteBeskjedQuery)
}
fun Connection.deleteOppgaver(eventIds: List<String>) {
    deleteVarsler(eventIds, deleteOppgaveQuery)
}
fun Connection.deleteInnbokser(eventIds: List<String>) {
    deleteVarsler(eventIds, deleteInnboksQuery)
}
private fun Connection.deleteVarsler(eventIds: List<String>, deleteVarselQuery: String) {
    prepareStatement(deleteVarselQuery).use {
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
