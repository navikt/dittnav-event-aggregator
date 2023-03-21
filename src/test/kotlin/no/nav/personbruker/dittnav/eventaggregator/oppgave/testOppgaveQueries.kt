package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.personbruker.dittnav.eventaggregator.common.database.getListFromString
import no.nav.personbruker.dittnav.eventaggregator.common.database.getNullableLocalDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.database.getUtcDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import no.nav.personbruker.dittnav.eventaggregator.common.database.singleResult
import no.nav.personbruker.dittnav.eventaggregator.common.getFristUtløpt
import java.sql.Connection
import java.sql.ResultSet

fun Connection.getAllOppgave(): List<Oppgave> =
    prepareStatement("""SELECT * FROM oppgave""")
        .use {
            it.executeQuery().list {
                toOppgave()
            }
        }

fun Connection.getAllOppgaveByAktiv(aktiv: Boolean): List<Oppgave> =
    prepareStatement("""SELECT * FROM oppgave WHERE aktiv = ?""")
        .use {
            it.setBoolean(1, aktiv)
            it.executeQuery().list {
                toOppgave()
            }
        }

fun Connection.getOppgaveByEventId(eventId: String): Oppgave =
    prepareStatement("""SELECT * FROM oppgave WHERE eventId = ?""")
        .use {
            it.setString(1, eventId)
            it.executeQuery().singleResult {
                toOppgave()
            }
        }

fun Connection.deleteAllOppgave() =
        prepareStatement("""DELETE FROM OPPGAVE""")
                .use {it.execute()}

fun ResultSet.toOppgave() = Oppgave(
    systembruker = getString("systembruker"),
    namespace = getString("namespace"),
    appnavn = getString("appnavn"),
    eventTidspunkt = getUtcDateTime("eventTidspunkt"),
    forstBehandlet = getUtcDateTime("forstBehandlet"),
    fodselsnummer = getString("fodselsnummer"),
    eventId = getString("eventId"),
    grupperingsId = getString("grupperingsId"),
    tekst = getString("tekst"),
    link = getString("link"),
    sikkerhetsnivaa = getInt("sikkerhetsnivaa"),
    sistOppdatert = getUtcDateTime("sistOppdatert"),
    aktiv = getBoolean("aktiv"),
    eksternVarsling = getBoolean("eksternVarsling"),
    prefererteKanaler = getListFromString("prefererteKanaler", ","),
    synligFremTil = getNullableLocalDateTime("synligFremTil"),
    fristUtløpt = getFristUtløpt()
)
