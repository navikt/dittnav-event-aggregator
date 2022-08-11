package no.nav.personbruker.dittnav.eventaggregator.beskjed.archive

import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import java.time.LocalDateTime

class BeskjedArchivingRepository(private val database: Database) {
    suspend fun getBeskjedOlderThan(dateThreshold: LocalDateTime): List<BeskjedArchiveDTO> {
        return database.queryWithExceptionTranslation {
            getBeskjedArchiveDtoOlderThan(dateThreshold)
        }
    }

    suspend fun moveToArchive(toArchive: List<BeskjedArchiveDTO>) {
        database.queryWithExceptionTranslation {
            val eventIds = toArchive.map { it.eventId }

            createBeskjedInArchive(toArchive)
            deleteDoknotifikasjonStatusBeskjedWithEventIds(eventIds)
            deleteBeskjedWithEventIds(eventIds)
        }
    }
}
