package no.nav.personbruker.dittnav.eventaggregator.archive

import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import java.time.LocalDateTime

class VarselArchivingRepository(private val database: Database) {

    suspend fun archiveOldVarsler(varselType: VarselType, dateThreshold: LocalDateTime): List<VarselArchiveDTO> {
        val archivableVarsler = database.queryWithExceptionTranslation {
            getArchivableVarsler(varselType, dateThreshold)
        }
        if(archivableVarsler.isEmpty()) return emptyList()

        val eventIds = archivableVarsler.map { it.eventId }

        database.queryWithExceptionTranslation {
            createArchivedVarsler(varselType, archivableVarsler)
            deleteDoknotifikasjonStatus(varselType, eventIds)
            deleteVarsler(varselType, eventIds)
        }

        return archivableVarsler
    }
}
