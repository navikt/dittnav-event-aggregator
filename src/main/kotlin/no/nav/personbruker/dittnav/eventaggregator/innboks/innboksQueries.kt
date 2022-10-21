package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.executeBatchUpdateQuery
import no.nav.personbruker.dittnav.eventaggregator.common.database.executePersistQuery
import no.nav.personbruker.dittnav.eventaggregator.done.Done
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Types

private const val createQuery = """INSERT INTO innboks(systembruker, eventTidspunkt, forstBehandlet, fodselsnummer, eventId, grupperingsId, tekst, link, sikkerhetsnivaa, sistOppdatert, aktiv, eksternVarsling, prefererteKanaler, namespace, appnavn)
            VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

fun Connection.createInnboks(innboks: Innboks): PersistActionResult =
        executePersistQuery(createQuery) {
            buildStatementForSingleRow(innboks)
        }

private fun PreparedStatement.buildStatementForSingleRow(innboks: Innboks) {
    setString(1, innboks.systembruker)
    setObject(2, innboks.eventTidspunkt, Types.TIMESTAMP)
    setObject(3, innboks.forstBehandlet, Types.TIMESTAMP)
    setString(4, innboks.fodselsnummer)
    setString(5, innboks.eventId)
    setString(6, innboks.grupperingsId)
    setString(7, innboks.tekst)
    setString(8, innboks.link)
    setInt(9, innboks.sikkerhetsnivaa)
    setObject(10, innboks.sistOppdatert, Types.TIMESTAMP)
    setBoolean(11, innboks.aktiv)
    setBoolean(12, innboks.eksternVarsling)
    setObject(13, innboks.prefererteKanaler.joinToString(","))
    setString(14, innboks.namespace)
    setString(15, innboks.appnavn)
}

fun Connection.setInnboksEventerAktivFlag(doneEvents: List<Done>, aktiv: Boolean) {
    executeBatchUpdateQuery("""UPDATE innboks SET aktiv = ?, sistoppdatert = ? WHERE eventId = ?""") {
        doneEvents.forEach { done ->
            setBoolean(1, aktiv)
            setObject(2, nowAtUtc(), Types.TIMESTAMP)
            setString(3, done.eventId)
            addBatch()
        }
    }
}