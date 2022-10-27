package no.nav.personbruker.dittnav.eventaggregator.archive

import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import java.time.LocalDateTime

class VarselArchivingRepository(private val database: Database) {

    suspend fun archiveOldBeskjeder(dateThreshold: LocalDateTime): List<BrukernotifikasjonArchiveDTO> {
        val archivableBeskjeder = database.queryWithExceptionTranslation {
            getArchivableBeskjeder(dateThreshold)
        }
        if(archivableBeskjeder.isEmpty()) return emptyList()

        val eventIds = archivableBeskjeder.map { it.eventId }

        database.queryWithExceptionTranslation {
            createArchivedBeskjeder(archivableBeskjeder)
            deleteDoknotifikasjonStatusVarselBeskjed(eventIds)
            deleteBeskjeder(eventIds)
        }

        return archivableBeskjeder
    }

    suspend fun archiveOldOppgaver(dateThreshold: LocalDateTime): List<BrukernotifikasjonArchiveDTO> {
        val archivableOppgaver = database.queryWithExceptionTranslation {
            getArchivableOppgaver(dateThreshold)
        }
        if(archivableOppgaver.isEmpty()) return emptyList()

        val eventIds = archivableOppgaver.map { it.eventId }

        database.queryWithExceptionTranslation {
            createArchivedOppgaver(archivableOppgaver)
            deleteDoknotifikasjonStatusVarselOppgave(eventIds)
            deleteOppgaver(eventIds)
        }

        return archivableOppgaver
    }

    suspend fun archiveOldInnbokser(dateThreshold: LocalDateTime): List<BrukernotifikasjonArchiveDTO> {
        val archivableInnbokser = database.queryWithExceptionTranslation {
            getArchivableInnbokser(dateThreshold)
        }
        if(archivableInnbokser.isEmpty()) return emptyList()

        val eventIds = archivableInnbokser.map { it.eventId }

        database.queryWithExceptionTranslation {
            createArchivedInnbokser(archivableInnbokser)
            deleteDoknotifikasjonStatusVarselInnboks(eventIds)
            deleteInnbokser(eventIds)
        }

        return archivableInnbokser
    }
}
