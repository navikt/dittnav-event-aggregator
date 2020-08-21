package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonRepository
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.persistEachIndividuallyAndAggregateResults

class StatusoppdateringRepository(private val database: Database) : BrukernotifikasjonRepository<Statusoppdatering> {

    override suspend fun createInOneBatch(entities: List<Statusoppdatering>): ListPersistActionResult<Statusoppdatering> {
        return database.queryWithExceptionTranslation {
            createStatusoppdateringer(entities)
        }
    }

    override suspend fun createOneByOneToFilterOutTheProblematicEvents(entities: List<Statusoppdatering>): ListPersistActionResult<Statusoppdatering> {
        return database.queryWithExceptionTranslation {
            entities.persistEachIndividuallyAndAggregateResults { entity ->
                createStatusoppdatering(entity)
            }
        }
    }
}