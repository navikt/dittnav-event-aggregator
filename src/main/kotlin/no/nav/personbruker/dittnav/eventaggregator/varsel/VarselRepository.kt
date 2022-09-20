package no.nav.personbruker.dittnav.eventaggregator.varsel

import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.createBeskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.setBeskjederAktivflagg
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.Brukernotifikasjon
import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.getBrukernotifikasjonFromViewForEventIds
import no.nav.personbruker.dittnav.eventaggregator.done.Done
import no.nav.personbruker.dittnav.eventaggregator.done.createDoneEvent
import no.nav.personbruker.dittnav.eventaggregator.innboks.Innboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.createInnboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.setInnboksEventerAktivFlag
import no.nav.personbruker.dittnav.eventaggregator.oppgave.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.createOppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.setOppgaverAktivFlag

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

    suspend fun inaktiverBeskjed(done: Done) {
        database.queryWithExceptionTranslation {
            setBeskjederAktivflagg(listOf(done), false)
        }
    }

    suspend fun inaktiverOppgave(done: Done) {
        database.queryWithExceptionTranslation {
            setOppgaverAktivFlag(listOf(done), false)
        }
    }

    suspend fun inaktiverInnboks(done: Done) {
        database.queryWithExceptionTranslation {
            setInnboksEventerAktivFlag(listOf(done), false)
        }
    }

    suspend fun getBrukernotifikasjoner(eventId: String): List<Brukernotifikasjon> {
        return database.queryWithExceptionTranslation {
            getBrukernotifikasjonFromViewForEventIds(listOf(eventId))
        }
    }
}