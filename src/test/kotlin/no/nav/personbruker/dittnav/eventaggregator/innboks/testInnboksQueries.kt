package no.nav.personbruker.dittnav.eventaggregator.innboks

import java.sql.Connection

fun Connection.deleteAllInnboks() =
        prepareStatement("""DELETE FROM INNBOKS""").execute()

fun Connection.deleteInnboksWithEventId(eventId: String) =
        prepareStatement("""DELETE FROM INNBOKS WHERE eventId = ?""")
                .use {
                    it.setString(1, eventId)
                    it.execute()
                }
