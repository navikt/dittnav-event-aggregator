package no.nav.personbruker.dittnav.eventaggregator.archive

import no.nav.personbruker.dittnav.eventaggregator.common.database.getUtcDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import java.sql.Connection
import java.sql.ResultSet

fun Connection.getAllArchivedBeskjed(): List<VarselArchiveDTO> =
    prepareStatement("""SELECT * FROM beskjed_arkiv""")
        .use {
            it.executeQuery().list {
                toArchiveDto(VarselType.BESKJED)
            }
        }

fun Connection.getAllArchivedOppgave(): List<VarselArchiveDTO> =
    prepareStatement("""SELECT * FROM oppgave_arkiv""")
        .use {
            it.executeQuery().list {
                toArchiveDto(VarselType.OPPGAVE)
            }
        }

fun Connection.getAllArchivedInnboks(): List<VarselArchiveDTO> =
    prepareStatement("""SELECT * FROM innboks_arkiv""")
        .use {
            it.executeQuery().list {
                toArchiveDto(VarselType.INNBOKS)
            }
        }

fun ResultSet.toArchiveDto(varseltype: VarselType) =
    VarselArchiveDTO(
        eventId = getString("eventid"),
        fodselsnummer = getString("fodselsnummer"),
        tekst = getString("tekst"),
        link = getString("link"),
        sikkerhetsnivaa = getInt("sikkerhetsnivaa"),
        aktiv = getBoolean("aktiv"),
        produsentApp = getString("produsentApp"),
        eksternVarslingSendt = getBoolean("eksternVarslingSendt"),
        eksternVarslingKanaler = getString("eksternVarslingKanaler"),
        forstBehandlet = getUtcDateTime("forstbehandlet"),
        fristUtløpt = getFristUtløpt(varseltype)
    )

private fun ResultSet.getFristUtløpt(varseltype: VarselType): Boolean? {
    if (varseltype == VarselType.INNBOKS) return null

    val result = getBoolean("frist_utløpt")
    return if (wasNull()) null else result
}
