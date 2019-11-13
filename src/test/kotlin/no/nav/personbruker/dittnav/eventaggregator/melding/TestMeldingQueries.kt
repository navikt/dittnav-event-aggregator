package no.nav.personbruker.dittnav.eventaggregator.melding

import java.sql.Connection

fun Connection.deleteAllMelding() =
        prepareStatement("""DELETE FROM MELDING""").execute()
