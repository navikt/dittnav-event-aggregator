package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.singleResult
import java.sql.Connection

fun Connection.getAllStatusoppdatering(): List<Statusoppdatering> =
    prepareStatement("""SELECT * FROM statusoppdatering""")
        .use {
            it.executeQuery().list {
                toStatusoppdatering()
            }
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

fun Connection.deleteAllStatusoppdatering() =
        prepareStatement("""DELETE FROM STATUSOPPDATERING""")
                .use { it.execute() }

fun Connection.deleteStatusoppdateringWithEventId(eventId: String) =
        prepareStatement("""DELETE FROM STATUSOPPDATERING WHERE eventId = ?""")
                .use {
                    it.setString(1, eventId)
                    it.execute()
                }
