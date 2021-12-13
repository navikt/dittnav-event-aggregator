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

private val createQuery = """INSERT INTO statusoppdatering (systembruker, eventId, eventTidspunkt, fodselsnummer, grupperingsId, link, sikkerhetsnivaa, sistOppdatert, statusGlobal, statusIntern, sakstema, namespace, appnavn)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

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
    setString(4, statusoppdatering.fodselsnummer)
    setString(5, statusoppdatering.grupperingsId)
    setString(6, statusoppdatering.link)
    setInt(7, statusoppdatering.sikkerhetsnivaa)
    setObject(8, statusoppdatering.sistOppdatert, Types.TIMESTAMP)
    setString(9, statusoppdatering.statusGlobal)
    setString(10, statusoppdatering.statusIntern)
    setString(11, statusoppdatering.sakstema)
    setString(12, statusoppdatering.namespace)
    setString(13, statusoppdatering.appnavn)
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
