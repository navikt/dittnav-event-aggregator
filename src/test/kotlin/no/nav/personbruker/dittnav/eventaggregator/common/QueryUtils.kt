package no.nav.personbruker.dittnav.eventaggregator.common

import java.sql.ResultSet

fun ResultSet.getFristUtløpt(): Boolean? {

    val result = getBoolean("frist_utløpt")
    return if (wasNull()) null else result
}