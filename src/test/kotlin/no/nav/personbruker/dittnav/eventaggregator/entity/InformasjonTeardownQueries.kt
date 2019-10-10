package no.nav.personbruker.dittnav.eventaggregator.entity

import java.sql.Connection

fun Connection.deleteAllRowsInInformasjon() =
        prepareStatement("""DELETE FROM INFORMASJON""")
                .use {
                    it.executeUpdate()
                }
