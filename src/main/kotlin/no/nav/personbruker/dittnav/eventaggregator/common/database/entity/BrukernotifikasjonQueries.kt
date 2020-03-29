package no.nav.personbruker.dittnav.eventaggregator.common.database.entity

import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
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

private fun ResultSet.toBrukernotifikasjon(): Brukernotifikasjon {
    return Brukernotifikasjon(
            eventId = getString("eventId"),
            produsent = getString("produsent"),
            type = EventType.valueOf(getString("type").toUpperCase()),
            fodselsnummer = getString("fodselsnummer")
    )
}
