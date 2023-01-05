package no.nav.personbruker.dittnav.eventaggregator.archive

import no.nav.personbruker.dittnav.eventaggregator.common.database.getUtcDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import java.sql.Connection
import java.sql.ResultSet

fun Connection.getAllArchivedBeskjed(): List<VarselArchiveDTO> =
    prepareStatement("""SELECT * FROM beskjed_arkiv""")
        .use {
            it.executeQuery().list {
                toArchiveDto()
            }
        }

fun Connection.getAllArchivedOppgave(): List<VarselArchiveDTO> =
    prepareStatement("""SELECT * FROM oppgave_arkiv""")
        .use {
            it.executeQuery().list {
                toArchiveDto()
            }
        }

fun Connection.getAllArchivedInnboks(): List<VarselArchiveDTO> =
    prepareStatement("""SELECT * FROM innboks_arkiv""")
        .use {
            it.executeQuery().list {
                toArchiveDto()
            }
        }

fun ResultSet.toArchiveDto() =
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
    )
