package no.nav.personbruker.dittnav.eventaggregator.oppgave.archive

import no.nav.personbruker.dittnav.eventaggregator.archive.BrukernotifikasjonArchiveDTO
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import java.time.LocalDateTime

class OppgaveArchivingRepository(private val database: Database) {
    suspend fun getOldOppgaveAsArchiveDto(dateThreshold: LocalDateTime): List<BrukernotifikasjonArchiveDTO> {
        return database.queryWithExceptionTranslation {
            getOppgaveAsArchiveDtoOlderThan(dateThreshold)
        }
    }

    suspend fun moveToOppgaveArchive(toArchive: List<BrukernotifikasjonArchiveDTO>) {

        val eventIds = toArchive.map { it.eventId }

        database.queryWithExceptionTranslation {
            createOppgaveInArchive(toArchive)
            deleteDoknotifikasjonStatusOppgaveWithEventIds(eventIds)
            deleteOppgaveWithEventIds(eventIds)
        }
    }
}
