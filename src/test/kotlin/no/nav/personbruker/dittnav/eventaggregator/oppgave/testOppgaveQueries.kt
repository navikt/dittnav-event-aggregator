package no.nav.personbruker.dittnav.eventaggregator.oppgave

import java.sql.Connection

fun Connection.deleteAllOppgave() =
        prepareStatement("""DELETE FROM YTEST_OPPGAVE""")
                .use {it.execute()}

fun Connection.deleteOppgaveWithEventId(eventId: String) =
        prepareStatement("""DELETE FROM YTEST_OPPGAVE WHERE eventId = ?""")
                .use {
                    it.setString(1, eventId)
                    it.execute()
                }
