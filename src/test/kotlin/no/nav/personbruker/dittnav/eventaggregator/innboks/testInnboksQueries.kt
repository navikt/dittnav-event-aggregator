package no.nav.personbruker.dittnav.eventaggregator.innboks

import java.sql.Connection

fun Connection.deleteAllInnboks() =
        prepareStatement("""DELETE FROM INNBOKS""").execute()
