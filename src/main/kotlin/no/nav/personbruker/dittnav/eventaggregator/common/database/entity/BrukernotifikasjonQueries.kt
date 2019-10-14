package no.nav.personbruker.dittnav.eventaggregator.common.database.entity

import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import java.sql.Connection
import java.sql.ResultSet

fun Connection.getAllBrukernotifikasjonFromView(): List<Brukernotifikasjon> =
        prepareStatement("""SELECT * FROM BRUKERNOTIFIKASJON_VIEW""")
                .use {
                    it.executeQuery().list {
                        toBrukernotifikasjon()
                    }
                }

private fun ResultSet.toBrukernotifikasjon(): Brukernotifikasjon {
    return Brukernotifikasjon(
            id = getString("eventId"),
            produsent = getString("produsent"),
            type = EventType.valueOf(getString("type").toUpperCase())
    )
}
