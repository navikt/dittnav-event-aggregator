package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType

class EksternVarslingStatusRepository(private val database: Database) {

    suspend fun getStatusIfExists(eventId: String, varselType: VarselType): EksternVarslingStatus? {
        return database.queryWithExceptionTranslation {
            getEksternVarslingStatusIfExists(eventId, varselType)
        }
    }

    suspend fun updateStatus(dokStatus: EksternVarslingStatus, varselType: VarselType) {
        database.queryWithExceptionTranslation {
            upsertEksternVarslingStatus(dokStatus, varselType)
        }
    }
}
