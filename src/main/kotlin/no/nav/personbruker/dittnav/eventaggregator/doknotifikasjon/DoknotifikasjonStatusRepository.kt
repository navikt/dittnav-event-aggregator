package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import no.nav.doknotifikasjon.schemas.DoknotifikasjonStatus
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult

class DoknotifikasjonStatusRepository(private val database: Database) {
    suspend fun updateStatusesForBeskjed(dokStatuses: List<DoknotifikasjonStatus>): ListPersistActionResult<DoknotifikasjonStatus> {
        return database.queryWithExceptionTranslation {
            upsertDoknotifikasjonStatusForBeskjed(dokStatuses)
        }
    }

    suspend fun updateStatusesForOppgave(dokStatuses: List<DoknotifikasjonStatus>): ListPersistActionResult<DoknotifikasjonStatus>  {
        return database.queryWithExceptionTranslation {
            upsertDoknotifikasjonStatusForOppgave(dokStatuses)
        }
    }
}
