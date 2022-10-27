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

private enum class VarselTableName {
    beskjed, oppgave, innboks
}

private fun getVarselToArchiveQuery(varselName: VarselTableName) = """
    SELECT 
      varsel.fodselsnummer,
      varsel.eventId,
      varsel.tekst,
      varsel.link,
      varsel.sikkerhetsnivaa,
      varsel.aktiv,
      varsel.appnavn,
      varsel.forstBehandlet,
      dns.status as dns_status,
      dns.kanaler as dns_kanaler
    FROM
      $varselName as varsel
        LEFT JOIN doknotifikasjon_status_$varselName as dns ON varsel.eventId = dns.eventId
    WHERE
      varsel.forstBehandlet between ? and ?
    limit 1000
"""

private fun insertVarselArchiveQuery(varselName: VarselTableName) = """
    INSERT INTO ${varselName}_arkiv (fodselsnummer, eventid, tekst, link, sikkerhetsnivaa, aktiv, produsentApp, eksternVarslingSendt, eksternVarslingKanaler, forstbehandlet, arkivert)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
"""

private fun deleteVarselQuery(varselName: VarselTableName) = """
    DELETE FROM $varselName WHERE eventId = ANY(?)
"""

private fun deleteDoknotifikasjonStatusQuery(varselName: VarselTableName) = """
    DELETE FROM doknotifikasjon_status_$varselName WHERE eventId = ANY(?)
"""

fun Connection.getArchivableBeskjeder(dateThreshold: LocalDateTime) = getVarselAsArchiveDtoOlderThan(dateThreshold, getVarselToArchiveQuery(VarselTableName.beskjed))
fun Connection.getArchivableOppgaver(dateThreshold: LocalDateTime) = getVarselAsArchiveDtoOlderThan(dateThreshold, getVarselToArchiveQuery(VarselTableName.oppgave))
fun Connection.getArchivableInnbokser(dateThreshold: LocalDateTime) = getVarselAsArchiveDtoOlderThan(dateThreshold, getVarselToArchiveQuery(VarselTableName.innboks))

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
    createVarselInArchive(toArchive, insertVarselArchiveQuery(VarselTableName.beskjed))
}
fun Connection.createArchivedOppgaver(toArchive: List<BrukernotifikasjonArchiveDTO>) {
    createVarselInArchive(toArchive, insertVarselArchiveQuery(VarselTableName.oppgave))
}
fun Connection.createArchivedInnbokser(toArchive: List<BrukernotifikasjonArchiveDTO>) {
    createVarselInArchive(toArchive, insertVarselArchiveQuery(VarselTableName.innboks))
}
private fun Connection.createVarselInArchive(toArchive: List<BrukernotifikasjonArchiveDTO>, insertVarselArchiveQuery: String) {
    prepareStatement(insertVarselArchiveQuery).use { statement ->
        toArchive.forEach {
            statement.setParametersForSingleRow(it)
            statement.addBatch()
        }
        statement.executeBatch()
    }
}

fun Connection.deleteDoknotifikasjonStatusVarselBeskjed(eventIds: List<String>) {
    deleteDoknotifikasjonStatusVarsel(eventIds, deleteDoknotifikasjonStatusQuery(VarselTableName.beskjed))
}
fun Connection.deleteDoknotifikasjonStatusVarselOppgave(eventIds: List<String>) {
    deleteDoknotifikasjonStatusVarsel(eventIds, deleteDoknotifikasjonStatusQuery(VarselTableName.oppgave))
}
fun Connection.deleteDoknotifikasjonStatusVarselInnboks(eventIds: List<String>) {
    deleteDoknotifikasjonStatusVarsel(eventIds, deleteDoknotifikasjonStatusQuery(VarselTableName.innboks))
}
private fun Connection.deleteDoknotifikasjonStatusVarsel(eventIds: List<String>, deleteDoknotifikasjonStatusQuery: String) {
    prepareStatement(deleteDoknotifikasjonStatusQuery).use {
        it.setArray(1, toVarcharArray(eventIds))
        it.executeUpdate()
    }
}

fun Connection.deleteBeskjeder(eventIds: List<String>) {
    deleteVarsler(eventIds, deleteVarselQuery(VarselTableName.beskjed))
}
fun Connection.deleteOppgaver(eventIds: List<String>) {
    deleteVarsler(eventIds, deleteVarselQuery(VarselTableName.oppgave))
}
fun Connection.deleteInnbokser(eventIds: List<String>) {
    deleteVarsler(eventIds, deleteVarselQuery(VarselTableName.innboks))
}
private fun Connection.deleteVarsler(eventIds: List<String>, deleteVarselQuery: String) {
    prepareStatement(deleteVarselQuery).use {
        it.setArray(1, toVarcharArray(eventIds))
        it.executeUpdate()
    }
}

private fun PreparedStatement.setParametersForSingleRow(varselArchiveDTO: BrukernotifikasjonArchiveDTO) {
    setString(1, varselArchiveDTO.fodselsnummer)
    setString(2, varselArchiveDTO.eventId)
    setString(3, varselArchiveDTO.tekst)
    setString(4, varselArchiveDTO.link)
    setInt(5, varselArchiveDTO.sikkerhetsnivaa)
    setBoolean(6, varselArchiveDTO.aktiv)
    setString(7, varselArchiveDTO.produsentApp)
    setBoolean(8, varselArchiveDTO.eksternVarslingSendt)
    setString(9, varselArchiveDTO.eksternVarslingKanaler)
    setObject(10, varselArchiveDTO.forstBehandlet, Types.TIMESTAMP)
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