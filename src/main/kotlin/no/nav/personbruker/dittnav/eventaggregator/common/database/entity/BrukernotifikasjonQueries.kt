package no.nav.personbruker.dittnav.eventaggregator.common.database.entity

import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import java.sql.Array
import java.sql.Connection
import java.sql.ResultSet

fun Connection.getBrukernotifikasjonFromViewForEventIds(eventIds: List<String>): List<Brukernotifikasjon> =
        prepareStatement("""SELECT brukernotifikasjon_view.* FROM brukernotifikasjon_view WHERE eventid = ANY(?)""")
                .use {
                    it.setArray(1, toVarcharArray(eventIds))
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
