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

fun Connection.getAllInnboksByAktiv(aktiv: Boolean): List<Innboks> =
    prepareStatement("""SELECT * FROM innboks WHERE aktiv = ?""")
        .use {
            it.setBoolean(1, aktiv)
            it.executeQuery().list {
                toInnboks()
            }
        }

fun Connection.getInnboksByFodselsnummer(fodselsnummer: String): List<Innboks> =
    prepareStatement("""SELECT * FROM innboks WHERE fodselsnummer = ?""")
        .use {
            it.setString(1, fodselsnummer)
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

fun Connection.deleteInnboksWithEventId(eventId: String) =
        prepareStatement("""DELETE FROM INNBOKS WHERE eventId = ?""")
                .use {
                    it.setString(1, eventId)
                    it.execute()
                }
