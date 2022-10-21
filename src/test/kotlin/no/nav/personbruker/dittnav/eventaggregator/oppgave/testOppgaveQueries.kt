package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import no.nav.personbruker.dittnav.eventaggregator.common.database.singleResult
import java.sql.Connection

fun Connection.getAllOppgave(): List<Oppgave> =
    prepareStatement("""SELECT * FROM oppgave""")
        .use {
            it.executeQuery().list {
                toOppgave()
            }
        }

fun Connection.getAllOppgaveByAktiv(aktiv: Boolean): List<Oppgave> =
    prepareStatement("""SELECT * FROM oppgave WHERE aktiv = ?""")
        .use {
            it.setBoolean(1, aktiv)
            it.executeQuery().list {
                toOppgave()
            }
        }

fun Connection.getOppgaveByEventId(eventId: String): Oppgave =
    prepareStatement("""SELECT * FROM oppgave WHERE eventId = ?""")
        .use {
            it.setString(1, eventId)
            it.executeQuery().singleResult {
                toOppgave()
            }
        }

fun Connection.deleteAllOppgave() =
        prepareStatement("""DELETE FROM OPPGAVE""")
                .use {it.execute()}
