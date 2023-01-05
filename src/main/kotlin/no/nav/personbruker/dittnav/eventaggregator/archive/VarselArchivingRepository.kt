package no.nav.personbruker.dittnav.eventaggregator.archive

import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import java.time.LocalDateTime

class VarselArchivingRepository(private val database: Database) {

    suspend fun archiveOldVarsler(varselType: VarselType, dateThreshold: LocalDateTime): List<VarselArchiveDTO> {
        val archivableBeskjeder = database.queryWithExceptionTranslation {
            getArchivableVarsler(varselType, dateThreshold)
        }
        if(archivableBeskjeder.isEmpty()) return emptyList()

        val eventIds = archivableBeskjeder.map { it.eventId }

        database.queryWithExceptionTranslation {
            createArchivedVarsler(varselType, archivableBeskjeder)
            deleteDoknotifikasjonStatus(varselType, eventIds)
            deleteVarsler(varselType, eventIds)
        }

        return archivableBeskjeder
    }
}
