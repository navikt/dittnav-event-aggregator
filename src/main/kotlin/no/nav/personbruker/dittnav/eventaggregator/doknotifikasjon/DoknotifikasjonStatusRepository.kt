package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import no.nav.doknotifikasjon.schemas.DoknotifikasjonStatus
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult

class DoknotifikasjonStatusRepository(private val database: Database) {

    suspend fun getStatusesForBeskjed(eventIds: List<String>): List<DoknotifikasjonStatusDto> {
        return database.queryWithExceptionTranslation {
            getDoknotifikasjonStatusesForBeskjed(eventIds)
        }
    }

    suspend fun getStatusesForOppgave(eventIds: List<String>): List<DoknotifikasjonStatusDto> {
        return database.queryWithExceptionTranslation {
            getDoknotifikasjonStatusesForOppgave(eventIds)
        }
    }

    suspend fun getStatusesForInnboks(eventIds: List<String>): List<DoknotifikasjonStatusDto> {
        return database.queryWithExceptionTranslation {
            getDoknotifikasjonStatusesForInnboks(eventIds)
        }
    }

    suspend fun updateStatusesForBeskjed(dokStatuses: List<DoknotifikasjonStatusDto>): ListPersistActionResult<DoknotifikasjonStatusDto> {
        return database.queryWithExceptionTranslation {
            upsertDoknotifikasjonStatusForBeskjed(dokStatuses)
        }
    }

    suspend fun updateStatusesForOppgave(dokStatuses: List<DoknotifikasjonStatusDto>): ListPersistActionResult<DoknotifikasjonStatusDto>  {
        return database.queryWithExceptionTranslation {
            upsertDoknotifikasjonStatusForOppgave(dokStatuses)
        }
    }

    suspend fun updateStatusesForInnboks(dokStatuses: List<DoknotifikasjonStatusDto>): ListPersistActionResult<DoknotifikasjonStatusDto>  {
        return database.queryWithExceptionTranslation {
            upsertDoknotifikasjonStatusForInnboks(dokStatuses)
        }
    }
}
