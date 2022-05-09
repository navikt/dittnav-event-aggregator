package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.*
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

fun Connection.getAllStatusoppdatering(): List<Statusoppdatering> =
        prepareStatement("""SELECT * FROM statusoppdatering""")
                .use {
                    it.executeQuery().list {
                        toStatusoppdatering()
                    }
                }

private val createQuery = """INSERT INTO statusoppdatering (systembruker, eventId, eventTidspunkt, forstBehandlet, fodselsnummer, grupperingsId, link, sikkerhetsnivaa, sistOppdatert, statusGlobal, statusIntern, sakstema, namespace, appnavn)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

fun Connection.createStatusoppdatering(statusoppdatering: Statusoppdatering): PersistActionResult =
        executePersistQuery(createQuery) {
            buildStatementForSingleRow(statusoppdatering)
        }

fun Connection.createStatusoppdateringer(statusoppdateringer: List<Statusoppdatering>): ListPersistActionResult<Statusoppdatering> =
        executeBatchPersistQuery(createQuery) {
            statusoppdateringer.forEach { statusoppdatering ->
                buildStatementForSingleRow(statusoppdatering)
                addBatch()
            }
        }.toBatchPersistResult(statusoppdateringer)

private fun PreparedStatement.buildStatementForSingleRow(statusoppdatering: Statusoppdatering) {
    setString(1, statusoppdatering.systembruker)
    setString(2, statusoppdatering.eventId)
    setObject(3, statusoppdatering.eventTidspunkt, Types.TIMESTAMP)
    setObject(4, statusoppdatering.forstBehandlet, Types.TIMESTAMP)
    setString(5, statusoppdatering.fodselsnummer)
    setString(6, statusoppdatering.grupperingsId)
    setString(7, statusoppdatering.link)
    setInt(8, statusoppdatering.sikkerhetsnivaa)
    setObject(9, statusoppdatering.sistOppdatert, Types.TIMESTAMP)
    setString(10, statusoppdatering.statusGlobal)
    setString(11, statusoppdatering.statusIntern)
    setString(12, statusoppdatering.sakstema)
    setString(13, statusoppdatering.namespace)
    setString(14, statusoppdatering.appnavn)
}

fun Connection.getStatusoppdateringByFodselsnummer(fodselsnummer: String): List<Statusoppdatering> =
        prepareStatement("""SELECT * FROM statusoppdatering WHERE fodselsnummer = ?""")
                .use {
                    it.setString(1, fodselsnummer)
                    it.executeQuery().list {
                        toStatusoppdatering()
                    }
                }

fun Connection.getStatusoppdateringById(id: Int): Statusoppdatering =
        prepareStatement("""SELECT * FROM statusoppdatering WHERE id = ?""")
                .use {
                    it.setInt(1, id)
                    it.executeQuery().singleResult() {
                        toStatusoppdatering()
                    }
                }

fun Connection.getStatusoppdateringByEventId(eventId: String): Statusoppdatering =
        prepareStatement("""SELECT * FROM statusoppdatering WHERE eventId = ?""")
                .use {
                    it.setString(1, eventId)
                    it.executeQuery().singleResult() {
                        toStatusoppdatering()
                    }
                }

private fun ResultSet.toStatusoppdatering(): Statusoppdatering {
    return Statusoppdatering(
            id = getInt("id"),
            systembruker = getString("systembruker"),
            namespace = getString("namespace"),
            appnavn = getString("appnavn"),
            eventId = getString("eventId"),
            eventTidspunkt = getUtcDateTime("eventTidspunkt"),
            forstBehandlet = getUtcDateTime("forstBehandlet"),
            fodselsnummer = getString("fodselsnummer"),
            grupperingsId = getString("grupperingsId"),
            link = getString("link"),
            sikkerhetsnivaa = getInt("sikkerhetsnivaa"),
            sistOppdatert = getUtcDateTime("sistOppdatert"),
            statusGlobal = getString("statusGlobal"),
            statusIntern = getString("statusIntern"),
            sakstema = getString("sakstema")
    )
}
