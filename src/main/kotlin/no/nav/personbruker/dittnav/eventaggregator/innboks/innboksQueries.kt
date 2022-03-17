package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.*
import no.nav.personbruker.dittnav.eventaggregator.done.Done
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

fun Connection.getAllInnboks(): List<Innboks> =
        prepareStatement("""SELECT * FROM innboks""")
                .use {
                    it.executeQuery().list {
                        toInnboks()
                    }
                }

fun Connection.getInnboksById(entityId: Int): Innboks =
        prepareStatement("""SELECT * FROM innboks WHERE id = ?""")
                .use {
                    it.setInt(1, entityId)
                    it.executeQuery().singleResult {
                        toInnboks()
                    }
                }

private val createQuery = """INSERT INTO innboks(systembruker, eventTidspunkt, fodselsnummer, eventId, grupperingsId, tekst, link, sikkerhetsnivaa, sistOppdatert, aktiv, namespace, appnavn)
            VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

fun Connection.createInnboksEventer(innboksEventer: List<Innboks>) =
        executeBatchPersistQuery(createQuery) {
            innboksEventer.forEach { innboks ->
                buildStatementForSingleRow(innboks)
                addBatch()
            }
        }.toBatchPersistResult(innboksEventer)

fun Connection.createInnboks(innboks: Innboks): PersistActionResult =
        executePersistQuery(createQuery) {
            buildStatementForSingleRow(innboks)
        }

private fun PreparedStatement.buildStatementForSingleRow(innboks: Innboks) {
    setString(1, innboks.systembruker)
    setObject(2, innboks.eventTidspunkt, Types.TIMESTAMP)
    setString(3, innboks.fodselsnummer)
    setString(4, innboks.eventId)
    setString(5, innboks.grupperingsId)
    setString(6, innboks.tekst)
    setString(7, innboks.link)
    setInt(8, innboks.sikkerhetsnivaa)
    setObject(9, innboks.sistOppdatert, Types.TIMESTAMP)
    setBoolean(10, innboks.aktiv)
    setString(11, innboks.namespace)
    setString(12, innboks.appnavn)
}

fun Connection.setInnboksEventerAktivFlag(doneEvents: List<Done>, aktiv: Boolean) {
    executeBatchUpdateQuery("""UPDATE innboks SET aktiv = ? WHERE eventId = ? AND fodselsnummer = ?""") {
        doneEvents.forEach { done ->
            setBoolean(1, aktiv)
            setString(2, done.eventId)
            setString(3, done.fodselsnummer)
            addBatch()
        }
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


private fun ResultSet.toInnboks(): Innboks {
    return Innboks(
            id = getInt("id"),
            systembruker = getString("systembruker"),
            namespace =  getString("namespace"),
            appnavn =  getString("appnavn"),
            eventTidspunkt = getUtcDateTime("eventTidspunkt"),
            fodselsnummer = getString("fodselsnummer"),
            eventId = getString("eventId"),
            grupperingsId = getString("grupperingsId"),
            tekst = getString("tekst"),
            link = getString("link"),
            sikkerhetsnivaa = getInt("sikkerhetsnivaa"),
            sistOppdatert = getUtcDateTime("sistOppdatert"),
            aktiv = getBoolean("aktiv")
    )
}
