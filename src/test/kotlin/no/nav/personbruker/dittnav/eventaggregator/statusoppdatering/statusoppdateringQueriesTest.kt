package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import java.sql.Connection

fun Connection.deleteAllStatusoppdatering() =
        prepareStatement("""DELETE FROM STATUSOPPDATERING""")
                .use { it.execute() }

fun Connection.deleteStatusoppdateringWithEventId(eventId: String) =
        prepareStatement("""DELETE FROM STATUSOPPDATERING WHERE eventId = ?""")
                .use {
                    it.setString(1, eventId)
                    it.execute()
                }
