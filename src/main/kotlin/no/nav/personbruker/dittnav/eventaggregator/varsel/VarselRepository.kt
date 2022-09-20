package no.nav.personbruker.dittnav.eventaggregator.varsel

import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.createBeskjed
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.innboks.Innboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.createInnboks
import no.nav.personbruker.dittnav.eventaggregator.oppgave.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.createOppgave

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
}