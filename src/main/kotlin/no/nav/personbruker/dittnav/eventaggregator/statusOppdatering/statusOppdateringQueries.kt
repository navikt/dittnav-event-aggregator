package no.nav.personbruker.dittnav.eventaggregator.statusOppdatering

import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.*
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

fun Connection.getAllStatusOppdatering(): List<StatusOppdatering> =
        prepareStatement("""SELECT * FROM statusOppdatering""")
                .use {
                    it.executeQuery().list {
                        toStatusOppdatering()
                    }
                }

private val createQuery = """INSERT INTO statusOppdatering (systembruker, eventTidspunkt, fodselsnummer, eventId, grupperingsId, link, sikkerhetsnivaa, sistOppdatert, statusGlobal, statusIntern, sakstema)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

fun Connection.createStatusOppdatering(statusOppdatering: StatusOppdatering): PersistActionResult =
        executePersistQuery(createQuery) {
            buildStatementForSingleRow(statusOppdatering)
        }

fun Connection.createStatusOppdateringer(statusOppdateringer: List<StatusOppdatering>): ListPersistActionResult<StatusOppdatering> =
        executeBatchPersistQuery(createQuery) {
            statusOppdateringer.forEach { statusOppdatering ->
                buildStatementForSingleRow(statusOppdatering)
                addBatch()
            }
        }.toBatchPersistResult(statusOppdateringer)

private fun PreparedStatement.buildStatementForSingleRow(statusOppdatering: StatusOppdatering) {
    setString(1, statusOppdatering.systembruker)
    setString(2, statusOppdatering.eventId)
    setObject(3, statusOppdatering.eventTidspunkt, Types.TIMESTAMP)
    setString(4, statusOppdatering.fodselsnummer)
    setString(5, statusOppdatering.grupperingsId)
    setString(6, statusOppdatering.link)
    setInt(7, statusOppdatering.sikkerhetsnivaa)
    setObject(8, statusOppdatering.sistOppdatert, Types.TIMESTAMP)
    setString(9, statusOppdatering.statusGlobal)
    setString(10, statusOppdatering.statusIntern)
    setString(11, statusOppdatering.sakstema)
}

fun Connection.getStatusOppdateringByFodselsnummer(fodselsnummer: String): List<StatusOppdatering> =
        prepareStatement("""SELECT * FROM statusOppdatering WHERE fodselsnummer = ?""")
                .use {
                    it.setString(1, fodselsnummer)
                    it.executeQuery().list {
                        toStatusOppdatering()
                    }
                }

fun Connection.getStatusOppdateringById(id: Int): StatusOppdatering =
        prepareStatement("""SELECT * FROM statusOppdatering WHERE id = ?""")
                .use {
                    it.setInt(1, id)
                    it.executeQuery().singleResult() {
                        toStatusOppdatering()
                    }
                }

fun Connection.getStatusOppdateringByEventId(eventId: String): StatusOppdatering =
        prepareStatement("""SELECT * FROM statusOppdatering WHERE eventId = ?""")
                .use {
                    it.setString(1, eventId)
                    it.executeQuery().singleResult() {
                        toStatusOppdatering()
                    }
                }

private fun ResultSet.toStatusOppdatering(): StatusOppdatering {
    return StatusOppdatering(
            id = getInt("id"),
            systembruker = getString("systembruker"),
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

