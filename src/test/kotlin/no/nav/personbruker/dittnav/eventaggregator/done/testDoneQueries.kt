package no.nav.personbruker.dittnav.eventaggregator.done

import java.sql.Connection

fun Connection.deleteAllDone() =
        prepareStatement("""DELETE FROM YTEST_DONE""")
                .use {it.execute()}
