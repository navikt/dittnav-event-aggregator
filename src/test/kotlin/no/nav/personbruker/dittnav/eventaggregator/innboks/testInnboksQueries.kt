package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.personbruker.dittnav.eventaggregator.common.database.getListFromSeparatedString
import no.nav.personbruker.dittnav.eventaggregator.common.database.getUtcDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import no.nav.personbruker.dittnav.eventaggregator.common.database.singleResult
import java.sql.Connection
import java.sql.ResultSet

fun Connection.getAllInnboks(): List<Innboks> =
    prepareStatement("""SELECT * FROM innboks""")
        .use {
            it.executeQuery().list {
                toInnboks()
            }
        }

fun Connection.getInnboksByEventId(eventId: String): Innboks =
    prepareStatement("""SELECT * FROM innboks WHERE eventId = ?""")
        .use {
            it.setString(1, eventId)
            it.executeQuery().singleResult {
                toInnboks()
            }
        }

fun Connection.deleteAllInnboks() =
        prepareStatement("""DELETE FROM INNBOKS""").execute()


fun ResultSet.toInnboks() = Innboks(
    id = getInt("id"),
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
    prefererteKanaler = getListFromSeparatedString("prefererteKanaler", ",")
)

