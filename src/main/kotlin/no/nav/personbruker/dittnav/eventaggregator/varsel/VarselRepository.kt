package no.nav.personbruker.dittnav.eventaggregator.varsel

import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.createBeskjed
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.executeBatchUpdateQuery
import no.nav.personbruker.dittnav.eventaggregator.common.database.list
import no.nav.personbruker.dittnav.eventaggregator.common.database.toVarcharArray
import no.nav.personbruker.dittnav.eventaggregator.done.Done
import no.nav.personbruker.dittnav.eventaggregator.done.createDoneEvent
import no.nav.personbruker.dittnav.eventaggregator.innboks.Innboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.createInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.setInnboksEventerAktivFlag
import no.nav.personbruker.dittnav.eventaggregator.oppgave.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.createOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.setOppgaverAktivFlag
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types

class VarselRepository(private val database: Database) {

    suspend fun persistBeskjed(beskjed: Beskjed) = database.queryWithExceptionTranslation {
        createBeskjed(beskjed)
    }

    suspend fun persistVarsel(innboks: Innboks) = database.queryWithExceptionTranslation {
        createInnboks(innboks)
    }

    suspend fun persistOppgave(oppgave: Oppgave) = database.queryWithExceptionTranslation {
        createOppgave(oppgave)
    }

    suspend fun persistWaitingDone(done: Done) = database.queryWithExceptionTranslation {
        createDoneEvent(done)
    }

    suspend fun inaktiverVarsel(done: Done, varselType: VarselType) {
        database.queryWithExceptionTranslation {
            setVarselInaktiv(done.eventId, varselType)
        }
    }
    suspend fun inaktiverInnboks(done: Done) {
        database.queryWithExceptionTranslation {
            setInnboksEventerAktivFlag(listOf(done), false)
        }
    }

    suspend fun getVarsel(eventId: String): List<VarselIdentifier> {
        return database.queryWithExceptionTranslation {
            getVarsler(listOf(eventId))
        }
    }
}
