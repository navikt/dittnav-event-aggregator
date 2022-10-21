package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import no.nav.personbruker.dittnav.eventaggregator.common.database.singleResult
import java.sql.Connection

fun Connection.getAllInnboks(): List<Innboks> =
    prepareStatement("""SELECT * FROM innboks""")
        .use {
            it.executeQuery().list {
                toInnboks()
            }
        }

fun Connection.getInnboksByEventId(eventId: String): Innboks =
    prepareStatement("""SELECT * FROM innboks WHERE eventId = ?""")
        .use {
            it.setString(1, eventId)
            it.executeQuery().singleResult {
                toInnboks()
            }
        }

fun Connection.deleteAllInnboks() =
        prepareStatement("""DELETE FROM INNBOKS""").execute()
