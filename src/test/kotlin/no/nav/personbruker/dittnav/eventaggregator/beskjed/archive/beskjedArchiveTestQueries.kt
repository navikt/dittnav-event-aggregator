package no.nav.personbruker.dittnav.eventaggregator.beskjed.archive

import no.nav.personbruker.dittnav.eventaggregator.archive.BrukernotifikasjonArchiveDTO
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.getUtcDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import java.sql.Connection
import java.sql.ResultSet

fun Connection.deleteAllBeskjedArchive() =
    prepareStatement("""DELETE FROM BESKJED_ARKIV""")
        .use {it.execute()}

fun Connection.getAllArchivedBeskjed(): List<BrukernotifikasjonArchiveDTO> =
    prepareStatement("""SELECT * FROM beskjed_arkiv""")
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
