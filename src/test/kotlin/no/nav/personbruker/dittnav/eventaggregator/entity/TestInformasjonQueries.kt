package no.nav.personbruker.dittnav.eventaggregator.entity

import java.sql.Connection

fun Connection.deleteAllInformasjon() =
        prepareStatement("""DELETE FROM INFORMASJON""")
                .use {it.execute()}
