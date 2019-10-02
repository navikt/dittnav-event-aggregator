package no.nav.personbruker.dittnav.eventaggregator.entity

import java.sql.Connection

fun Connection.deleteAllOppgave() =
        prepareStatement("""DELETE FROM OPPGAVE""")
                .use {it.execute()}
