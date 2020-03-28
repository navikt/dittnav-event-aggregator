package no.nav.personbruker.dittnav.eventaggregator.beskjed

import java.sql.Connection

fun Connection.deleteAllBeskjed() =
        prepareStatement("""DELETE FROM YTEST_BESKJED""")
                .use {it.execute()}

fun Connection.deleteBeskjedWithEventId(eventId: String) =
        prepareStatement("""DELETE FROM YTEST_BESKJED WHERE eventId = ?""")
                .use {
                    it.setString(1, eventId)
                    it.execute()
                }
