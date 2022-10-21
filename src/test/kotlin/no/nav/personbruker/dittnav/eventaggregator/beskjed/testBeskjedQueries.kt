package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import no.nav.personbruker.dittnav.eventaggregator.common.database.singleResult
import java.sql.Connection

fun Connection.getAllBeskjed(): List<Beskjed> =
    prepareStatement("""SELECT * FROM beskjed""")
        .use {
            it.executeQuery().list {
                toBeskjed()
            }
        }

fun Connection.getAllBeskjedByAktiv(aktiv: Boolean): List<Beskjed> =
    prepareStatement("""SELECT * FROM beskjed WHERE aktiv = ?""")
        .use {
            it.setBoolean(1, aktiv)
            it.executeQuery().list {
                toBeskjed()
            }
        }

fun Connection.getBeskjedByEventId(eventId: String): Beskjed =
    prepareStatement("""SELECT * FROM beskjed WHERE eventId = ?""")
        .use {
            it.setString(1, eventId)
            it.executeQuery().singleResult() {
                toBeskjed()
            }
        }

fun Connection.deleteAllBeskjed() =
        prepareStatement("""DELETE FROM BESKJED""")
                .use {it.execute()}