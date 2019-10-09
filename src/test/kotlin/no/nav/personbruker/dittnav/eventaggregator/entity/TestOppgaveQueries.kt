package no.nav.personbruker.dittnav.eventaggregator.entity

import java.sql.Connection

fun Connection.deleteAllOppgave() =
        prepareStatement("""DELETE FROM OPPGAVE""")
                .use {it.execute()}

fun Connection.deleteOppgaveWithEventId(eventId: String) =
        prepareStatement("""DELETE FROM OPPGAVE WHERE eventId = ?""")
                .use {
                    it.setString(1, eventId)
                    it.execute()
                }
