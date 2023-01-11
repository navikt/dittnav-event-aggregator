package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.Brukernotifikasjon
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.innboks.setInnboksEventerAktivFlag
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import no.nav.personbruker.dittnav.eventaggregator.varsel.setVarslerInaktiv
import java.time.LocalDateTime

class DoneRepository(private val database: Database) {

    suspend fun updateVarselTables(doneEvents: List<Done>, varselType: VarselType) {
        if (doneEvents.isNotEmpty()) {
            database.queryWithExceptionTranslation {
                setVarslerInaktiv(doneEvents, varselType)
            }
        }
    }

    suspend fun updateInnboksTable(doneEvents: List<Done>) {
        if (doneEvents.isEmpty()) {
            return
        }
        database.queryWithExceptionTranslation {
            setInnboksEventerAktivFlag(doneEvents, false)
        }
    }

    suspend fun fetchVarslerFromViewForEventIds(eventIds: List<String>): List<Brukernotifikasjon> {
        var resultat = emptyList<Brukernotifikasjon>()
        database.queryWithExceptionTranslation {
            resultat = getBrukernotifikasjonFromViewForEventIds(eventIds)
        }
        return resultat
    }

    suspend fun fetchAllDoneEventsWithLimit(): List<Done> {
        var resultat = emptyList<Done>()
        database.queryWithExceptionTranslation {
            resultat = getAllDoneEventWithLimit(2500)
        }
        return resultat
    }

    suspend fun deleteDoneEventsFromCache(doneEventsToDelete: List<Done>) {
        database.queryWithExceptionTranslation {
            deleteDoneEvents(doneEventsToDelete)
        }
    }

    suspend fun updateDoneEventsSistBehandlet(doneEvents: List<Done>, sistBehandlet: LocalDateTime) {
        database.queryWithExceptionTranslation {
            updateDoneSistbehandlet(doneEvents, sistBehandlet)
        }
    }
}
