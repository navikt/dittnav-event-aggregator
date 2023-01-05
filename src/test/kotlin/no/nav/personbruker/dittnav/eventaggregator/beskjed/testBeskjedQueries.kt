package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.personbruker.dittnav.eventaggregator.common.database.getListFromSeparatedString
import no.nav.personbruker.dittnav.eventaggregator.common.database.getNullableLocalDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.database.getUtcDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import no.nav.personbruker.dittnav.eventaggregator.common.database.singleResult
import java.sql.Connection
import java.sql.ResultSet

fun Connection.getAllBeskjed(): List<Beskjed> =
    prepareStatement("""SELECT * FROM beskjed""")
        .use {
            it.executeQuery().list {
                toBeskjed()
            }
        }


fun Connection.getAllBeskjedByAktiv(aktiv: Boolean): List<Beskjed> =
    prepareStatement("""SELECT * FROM beskjed WHERE aktiv = ?""")
        .use {
            it.setBoolean(1, aktiv)
            it.executeQuery().list {
                toBeskjed()
            }
        }

fun Connection.getBeskjedByEventId(eventId: String): Beskjed =
    prepareStatement("""SELECT * FROM beskjed WHERE eventId = ?""")
        .use {
            it.setString(1, eventId)
            it.executeQuery().singleResult() {
                toBeskjed()
            }
        }

fun Connection.deleteAllBeskjed() =
    prepareStatement("""DELETE FROM BESKJED""")
        .use { it.execute() }

fun ResultSet.toBeskjed() = Beskjed(
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
    synligFremTil = getNullableLocalDateTime("synligFremTil"),
    aktiv = getBoolean("aktiv"),
    eksternVarsling = getBoolean("eksternVarsling"),
    prefererteKanaler = getListFromSeparatedString("prefererteKanaler", ",")
)
