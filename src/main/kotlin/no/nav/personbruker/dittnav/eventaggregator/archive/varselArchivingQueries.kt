package no.nav.personbruker.dittnav.eventaggregator.archive

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.EPOCH_START
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.common.database.getUtcDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import no.nav.personbruker.dittnav.eventaggregator.common.database.toVarcharArray
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusEnum.FERDIGSTILT
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import java.time.LocalDateTime

internal enum class VarselTable {
    beskjed, oppgave, innboks;

    companion object {
        fun fromVarselType(varselType: VarselType): VarselTable {
            return when (varselType) {
                VarselType.OPPGAVE -> oppgave
                VarselType.BESKJED -> beskjed
                VarselType.INNBOKS -> innboks
            }
        }
    }
}


private fun deleteVarselQuery(varselTable: VarselTable) = """
    DELETE FROM ${varselTable.name} WHERE eventId = ANY(?)
"""

private fun deleteDoknotifikasjonStatusQuery(varselTable: VarselTable) = """
    DELETE FROM doknotifikasjon_status_${varselTable.name} WHERE eventId = ANY(?)
"""

fun Connection.getArchivableVarsler(varselType: VarselType, dateThreshold: LocalDateTime): List<VarselArchiveDTO> {
    return prepareStatement(SqlGet.queryString(varselType))
        .use {
            it.setObject(1, EPOCH_START, Types.TIMESTAMP)
            it.setObject(2, dateThreshold, Types.TIMESTAMP)
            it.executeQuery().list {
                toVarselArchiveDTO(varselType)
            }
        }
}


fun Connection.createArchivedVarsler(varselType: VarselType, toArchive: List<VarselArchiveDTO>) {
    prepareStatement(SqlInsert.queryString(varselType)).use { statement ->
        toArchive.forEach {
            statement.setParametersForSingleRow(varselType, it)
            statement.addBatch()
        }
        statement.executeBatch()
    }
}

fun Connection.deleteDoknotifikasjonStatus(varselType: VarselType, eventIds: List<String>) {
    prepareStatement(deleteDoknotifikasjonStatusQuery(VarselTable.fromVarselType(varselType))).use {
        it.setArray(1, toVarcharArray(eventIds))
        it.executeUpdate()
    }
}

fun Connection.deleteVarsler(varselType: VarselType, eventIds: List<String>) {
    prepareStatement(deleteVarselQuery(VarselTable.fromVarselType(varselType))).use {
        it.setArray(1, toVarcharArray(eventIds))
        it.executeUpdate()
    }
}

private fun PreparedStatement.setParametersForSingleRow(varselType: VarselType, varselArchiveDTO: VarselArchiveDTO) {
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
    if (varselType == VarselType.BESKJED || varselType == VarselType.OPPGAVE)
        varselArchiveDTO.fristUtløpt?.let { setBoolean(12, it) } ?: setNull(12, Types.BOOLEAN)
}

private fun ResultSet.toVarselArchiveDTO(varselType: VarselType): VarselArchiveDTO {
        return VarselArchiveDTO(
            fodselsnummer = getString("fodselsnummer"),
            eventId = getString("eventId"),
            tekst = getString("tekst"),
            link = getString("link"),
            sikkerhetsnivaa = getInt("sikkerhetsnivaa"),
            aktiv = getBoolean("aktiv"),
            produsentApp = getString("appnavn"),
            forstBehandlet = getUtcDateTime("forstBehandlet"),
            eksternVarslingSendt = getEksternVarslingSendt(),
            eksternVarslingKanaler = getEksternVarslingKanaler(),
            fristUtløpt =getFristUtløpt(varselType)
        )
}

private fun ResultSet.getEksternVarslingSendt(): Boolean = FERDIGSTILT.name == getString("dns_status")
private fun ResultSet.getEksternVarslingKanaler(): String = getString("dns_kanaler") ?: ""
private fun ResultSet.getFristUtløpt(varselType: VarselType): Boolean? {
    if (varselType == VarselType.INNBOKS) return null

    val result = getBoolean("frist_utløpt")
    return if (wasNull()) null else result
}

object SqlInsert {
    fun queryString(varselType: VarselType): String = when (varselType) {
        VarselType.BESKJED -> insertVarselArchiveQueryWithFristUtløpt(VarselTable.beskjed.name)
        VarselType.OPPGAVE -> insertVarselArchiveQueryWithFristUtløpt(VarselTable.oppgave.name)
        VarselType.INNBOKS -> insertVarselArchiveQuery(VarselTable.innboks.name)
    }

    private fun insertVarselArchiveQuery(varselName: String) = """
        INSERT INTO ${varselName}_arkiv (fodselsnummer, eventid, tekst, link, sikkerhetsnivaa, aktiv, produsentApp, eksternVarslingSendt, eksternVarslingKanaler, forstbehandlet, arkivert)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

    private fun insertVarselArchiveQueryWithFristUtløpt(varselName: String) = """
        INSERT INTO ${varselName}_arkiv (fodselsnummer, eventid, tekst, link, sikkerhetsnivaa, aktiv, produsentApp, eksternVarslingSendt, eksternVarslingKanaler, forstbehandlet, arkivert, frist_utløpt)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""
}

object SqlGet {
    fun queryString(varselType: VarselType): String = when (varselType) {
        VarselType.BESKJED -> getVarselToArchiveQueryWithFristUtløpt(VarselTable.beskjed.name)
        VarselType.OPPGAVE -> getVarselToArchiveQueryWithFristUtløpt(VarselTable.oppgave.name)
        VarselType.INNBOKS -> getVarselToArchiveQuery(VarselTable.innboks.name)
    }

    private fun getVarselToArchiveQuery(varselTable: String) = """
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
      $varselTable as varsel
        LEFT JOIN doknotifikasjon_status_$varselTable as dns ON varsel.eventId = dns.eventId
    WHERE
      varsel.forstBehandlet between ? and ?
    limit 1000
"""

    private fun getVarselToArchiveQueryWithFristUtløpt(varselTable: String) = """
    SELECT 
      varsel.fodselsnummer,
      varsel.eventId,
      varsel.tekst,
      varsel.link,
      varsel.sikkerhetsnivaa,
      varsel.aktiv,
      varsel.appnavn,
      varsel.forstBehandlet,
      varsel.frist_utløpt,
      dns.status as dns_status,
      dns.kanaler as dns_kanaler
    FROM
      $varselTable as varsel
        LEFT JOIN doknotifikasjon_status_${varselTable} as dns ON varsel.eventId = dns.eventId
    WHERE
      varsel.forstBehandlet between ? and ?
    limit 1000
"""
}