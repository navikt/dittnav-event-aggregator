package no.nav.personbruker.dittnav.eventaggregator.informasjon

import java.sql.Connection

fun Connection.deleteAllInformasjon() =
        prepareStatement("""DELETE FROM INFORMASJON""")
                .use {it.execute()}

fun Connection.deleteInformasjonWithEventId(eventId: String) =
        prepareStatement("""DELETE FROM INFORMASJON WHERE eventId = ?""")
                .use {
                    it.setString(1, eventId)
                    it.execute()
                }
