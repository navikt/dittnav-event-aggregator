package no.nav.personbruker.dittnav.eventaggregator.metrics.db

import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import java.sql.Connection
import java.sql.ResultSet

fun Connection.getProdusentnavn(): List<Produsent> =
        prepareStatement("""SELECT * FROM systembrukere""")
                .use {
                    it.executeQuery().list {
                        toProdusent()
                    }
                }

private fun ResultSet.toProdusent(): Produsent {
    return Produsent(
            systembruker = getString("systembruker"),
            produsentnavn = getString("produsentnavn")
    )
}
