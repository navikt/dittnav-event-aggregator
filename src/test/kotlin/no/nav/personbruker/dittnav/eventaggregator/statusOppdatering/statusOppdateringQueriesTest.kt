package no.nav.personbruker.dittnav.eventaggregator.statusOppdatering

import java.sql.Connection

fun Connection.deleteAllStatusOppdatering() =
        prepareStatement("""DELETE FROM STATUSOPPDATERING""")
                .use { it.execute() }

fun Connection.deleteStatusOppdateringWithEventId(eventId: String) =
        prepareStatement("""DELETE FROM STATUSOPPDATERING WHERE eventId = ?""")
                .use {
                    it.setString(1, eventId)
                    it.execute()
                }
