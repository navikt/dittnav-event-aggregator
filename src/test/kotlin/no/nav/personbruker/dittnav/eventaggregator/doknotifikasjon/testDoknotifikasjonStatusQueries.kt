package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import no.nav.personbruker.dittnav.eventaggregator.common.database.getUtcDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import java.sql.Connection
import java.sql.ResultSet

fun Connection.deleteAllDoknotifikasjonStatusBeskjed() {
    prepareStatement("""DELETE FROM ekstern_varsling_status_beskjed""")
        .use {it.execute()}
}

fun Connection.deleteAllDoknotifikasjonStatusOppgave() {
    prepareStatement("""DELETE FROM ekstern_varsling_status_oppgave""")
        .use {it.execute()}
}


fun Connection.deleteAllDoknotifikasjonStatusInnboks() {
    prepareStatement("""DELETE FROM ekstern_varsling_status_innboks""")
        .use {it.execute()}
}

fun Connection.countEksternVarslingStatusBeskjed(): Int {
    return prepareStatement("""SELECT count(*) as antall FROM ekstern_varsling_status_beskjed""")
        .use {
            it.executeQuery().run {
                next()
                getInt("antall")
            }
        }
}

fun Connection.countEksternVarslingStatusOppgave(): Int {
    return prepareStatement("""SELECT count(*) as antall FROM ekstern_varsling_status_oppgave""")
        .use {
            it.executeQuery().run {
                next()
                getInt("antall")
            }
        }
}

fun Connection.countEksternVarslingStatusInnboks(): Int {
    return prepareStatement("""SELECT count(*) as antall FROM ekstern_varsling_status_innboks""")
        .use {
            it.executeQuery().run {
                next()
                getInt("antall")
            }
        }
}
