package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.executeBatchUpdateQuery
import no.nav.personbruker.dittnav.eventaggregator.common.database.executePersistQuery
import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import no.nav.personbruker.dittnav.eventaggregator.done.Done
import no.nav.personbruker.dittnav.eventaggregator.varsel.HendelseType
import no.nav.personbruker.dittnav.eventaggregator.varsel.HendelseType.Inaktivert
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselHendelse
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType.BESKJED
import no.nav.personbruker.dittnav.eventaggregator.varsel.setVarselInaktiv
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Types

private const val createQuery =
    """INSERT INTO beskjed (systembruker, eventTidspunkt, forstBehandlet, fodselsnummer, eventId, grupperingsId, tekst, link, sikkerhetsnivaa, sistOppdatert, synligFremTil, aktiv, eksternVarsling, prefererteKanaler, namespace, appnavn,frist_utløpt)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""


fun Connection.createBeskjed(beskjed: Beskjed): PersistActionResult =
    executePersistQuery(createQuery) {
        setParametersForSingleRow(beskjed)
    }

private fun PreparedStatement.setParametersForSingleRow(beskjed: Beskjed) {
    setString(1, beskjed.systembruker)
    setObject(2, beskjed.eventTidspunkt, Types.TIMESTAMP)
    setObject(3, beskjed.forstBehandlet, Types.TIMESTAMP)
    setString(4, beskjed.fodselsnummer)
    setString(5, beskjed.eventId)
    setString(6, beskjed.grupperingsId)
    setString(7, beskjed.tekst)
    setString(8, beskjed.link)
    setInt(9, beskjed.sikkerhetsnivaa)
    setObject(10, beskjed.sistOppdatert, Types.TIMESTAMP)
    setObject(11, beskjed.synligFremTil, Types.TIMESTAMP)
    setBoolean(12, beskjed.aktiv)
    setBoolean(13, beskjed.eksternVarsling)
    setObject(14, beskjed.prefererteKanaler.joinToString(","))
    setString(15, beskjed.namespace)
    setString(16, beskjed.appnavn)
    beskjed.fristUtløpt?.let { setBoolean(17, it) } ?: setNull(17, Types.BOOLEAN)
}


fun Connection.setExpiredBeskjedAsInactive(): List<VarselHendelse> {
    return prepareStatement("""UPDATE beskjed SET aktiv = FALSE, sistoppdatert = ?, frist_utløpt = TRUE WHERE aktiv = TRUE AND synligFremTil < ? RETURNING eventId, namespace, appnavn""")
        .use {
            it.setObject(1, nowAtUtc(), Types.TIMESTAMP)
            it.setObject(2, nowAtUtc(), Types.TIMESTAMP)
            it.executeQuery().list {
                VarselHendelse(
                    Inaktivert,
                    BESKJED,
                    eventId = getString("eventId"),
                    namespace = getString("namespace"),
                    appnavn = getString("appnavn")
                )
            }
        }
}

class BeskjedNotFoundException(eventId: String) :
    IllegalArgumentException("beskjed med eventId $eventId ikke funnet")

class BeskjedDoesNotBelongToUserException(val eventId: String) : IllegalArgumentException()
