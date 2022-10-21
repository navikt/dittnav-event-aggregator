package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import no.nav.personbruker.dittnav.eventaggregator.common.database.getUtcDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import java.sql.Connection
import java.sql.ResultSet

fun Connection.deleteAllDoknotifikasjonStatusBeskjed() {
    prepareStatement("""DELETE FROM doknotifikasjon_status_beskjed""")
        .use {it.execute()}
}

fun Connection.deleteAllDoknotifikasjonStatusOppgave() {
    prepareStatement("""DELETE FROM doknotifikasjon_status_oppgave""")
        .use {it.execute()}
}


fun Connection.deleteAllDoknotifikasjonStatusInnboks() {
    prepareStatement("""DELETE FROM doknotifikasjon_status_innboks""")
        .use {it.execute()}
}

fun Connection.getAllDoknotifikasjonStatusBeskjed(): List<DoknotStatusDTO> {
    return prepareStatement("""SELECT * FROM doknotifikasjon_status_beskjed""")
        .use {
            it.executeQuery().list {
                toDoknotStatusDTO()
            }
        }
}

fun Connection.getAllDoknotifikasjonStatusOppgave(): List<DoknotStatusDTO> {
    return prepareStatement("""SELECT * FROM doknotifikasjon_status_oppgave""")
        .use {
            it.executeQuery().list {
                toDoknotStatusDTO()
            }
        }
}

fun Connection.getAllDoknotifikasjonStatusInnboks(): List<DoknotStatusDTO> {
    return prepareStatement("""SELECT * FROM doknotifikasjon_status_innboks""")
        .use {
            it.executeQuery().list {
                toDoknotStatusDTO()
            }
        }
}

private fun ResultSet.toDoknotStatusDTO() = DoknotStatusDTO(
    eventId = getString("eventId"),
    status = getString("status"),
    melding = getString("melding"),
    distribusjonsId = getLong("distribusjonsId"),
    antallOppdateringer = getInt("antall_oppdateringer"),
    tidspunkt = getUtcDateTime("tidspunkt"),
)
