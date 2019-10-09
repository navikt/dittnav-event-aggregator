package no.nav.personbruker.dittnav.eventaggregator.entity

import java.sql.Connection

fun Connection.deleteAllDone() =
        prepareStatement("""DELETE FROM DONE""")
                .use {it.execute()}
