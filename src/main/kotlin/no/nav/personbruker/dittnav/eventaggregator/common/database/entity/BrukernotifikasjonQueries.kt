package no.nav.personbruker.dittnav.eventaggregator.common.database.entity

import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import java.sql.Array
import java.sql.Connection
import java.sql.ResultSet

fun Connection.getBrukernotifikasjonFromViewByAktiv(aktiv: Boolean): List<Brukernotifikasjon> =
        prepareStatement("""SELECT * FROM brukernotifikasjon_view where aktiv = ?""")
                .use {
                    it.setBoolean(1, aktiv)
                    it.executeQuery().list {
                        toBrukernotifikasjon()
                    }
                }

fun Connection.getBrukernotifikasjonFromViewForEventIdsByAktiv(eventIds: List<String>, aktiv: Boolean): List<Brukernotifikasjon> =
        prepareStatement("""SELECT brukernotifikasjon_view.* FROM brukernotifikasjon_view 
                INNER JOIN unnest(?) as params(eventId) on brukernotifikasjon_view.eventId = params.eventId
                WHERE aktiv = ?""")
                .use {
                    it.setArray(1, toVarcharArray(eventIds))
                    it.setBoolean(2, aktiv)
                    it.executeQuery().list {
                        toBrukernotifikasjon()
                    }
                }

private fun ResultSet.toBrukernotifikasjon(): Brukernotifikasjon {
    return Brukernotifikasjon(
            eventId = getString("eventId"),
            systembruker = getString("systembruker"),
            type = EventType.valueOf(getString("type").toUpperCase()),
            fodselsnummer = getString("fodselsnummer")
    )
}


private fun Connection.toVarcharArray(stringList: List<String>): Array {
    return createArrayOf("VARCHAR", stringList.toTypedArray())
}
