package no.nav.personbruker.dittnav.eventaggregator.beskjed.archive

import no.nav.personbruker.dittnav.eventaggregator.archive.BrukernotifikasjonArchiveDTO
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import java.time.LocalDateTime

class BeskjedArchivingRepository(private val database: Database) {
    suspend fun getOldBeskjedAsArchiveDto(dateThreshold: LocalDateTime): List<BrukernotifikasjonArchiveDTO> {
        return database.queryWithExceptionTranslation {
            getBeskjedAsArchiveDtoOlderThan(dateThreshold)
        }
    }

    suspend fun moveToBeskjedArchive(toArchive: List<BrukernotifikasjonArchiveDTO>) {

        val eventIds = toArchive.map { it.eventId }

        database.queryWithExceptionTranslation {
            createBeskjedInArchive(toArchive)
            deleteDoknotifikasjonStatusBeskjedWithEventIds(eventIds)
            deleteBeskjedWithEventIds(eventIds)
        }
    }
}
