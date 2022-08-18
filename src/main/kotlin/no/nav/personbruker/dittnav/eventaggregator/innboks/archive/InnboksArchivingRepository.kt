package no.nav.personbruker.dittnav.eventaggregator.innboks.archive

import no.nav.personbruker.dittnav.eventaggregator.archive.BrukernotifikasjonArchiveDTO
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import java.time.LocalDateTime

class InnboksArchivingRepository(private val database: Database) {
    suspend fun getOldInnboksAsArchiveDto(dateThreshold: LocalDateTime): List<BrukernotifikasjonArchiveDTO> {
        return database.queryWithExceptionTranslation {
            getInnboksAsArchiveDtoOlderThan(dateThreshold)
        }
    }

    suspend fun moveToInnboksArchive(toArchive: List<BrukernotifikasjonArchiveDTO>) {

        val eventIds = toArchive.map { it.eventId }

        database.queryWithExceptionTranslation {
            createInnboksInArchive(toArchive)
            deleteDoknotifikasjonStatusInnboksWithEventIds(eventIds)
            deleteInnboksWithEventIds(eventIds)
        }
    }
}
