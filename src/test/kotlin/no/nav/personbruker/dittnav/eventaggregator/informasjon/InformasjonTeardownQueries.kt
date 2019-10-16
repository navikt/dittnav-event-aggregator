package no.nav.personbruker.dittnav.eventaggregator.informasjon

import java.sql.Connection

fun Connection.deleteAllRowsInInformasjon() =
        prepareStatement("""DELETE FROM INFORMASJON""")
                .use {
                    it.executeUpdate()
                }
