package no.nav.personbruker.dittnav.eventaggregator.innboks.archive

import no.nav.personbruker.dittnav.eventaggregator.archive.BrukernotifikasjonArchiveDTO
import no.nav.personbruker.dittnav.eventaggregator.common.database.getUtcDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import java.sql.Connection
import java.sql.ResultSet

fun Connection.deleteAllInnboksArchive() =
    prepareStatement("""DELETE FROM INNBOKS_ARKIV""")
        .use {it.execute()}

fun Connection.getAllArchivedInnboks(): List<BrukernotifikasjonArchiveDTO> =
    prepareStatement("""SELECT * FROM innboks_arkiv""")
        .use {
            it.executeQuery().list {
                toArchiveDto()
            }
        }

private fun ResultSet.toArchiveDto() =
    BrukernotifikasjonArchiveDTO(
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
    )
