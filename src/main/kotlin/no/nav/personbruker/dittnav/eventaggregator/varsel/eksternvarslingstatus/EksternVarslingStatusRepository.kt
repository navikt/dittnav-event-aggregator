package no.nav.personbruker.dittnav.eventaggregator.varsel.eksternvarslingstatus

import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusDto
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType

class EksternVarslingStatusRepository(private val database: Database) {

    suspend fun getStatusIfExists(eventId: String, varselType: VarselType): DoknotifikasjonStatusDto? {
        return database.queryWithExceptionTranslation {
            getStatusIfExists(eventId, varselType)
        }
    }

    suspend fun updateStatus(dokStatus: DoknotifikasjonStatusDto, varselType: VarselType) {
        database.queryWithExceptionTranslation {
            upsertDoknotifikasjonStatus(dokStatus, varselType)
        }
    }
}
