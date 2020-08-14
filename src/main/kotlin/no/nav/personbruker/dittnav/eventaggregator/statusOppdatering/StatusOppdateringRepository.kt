package no.nav.personbruker.dittnav.eventaggregator.statusOppdatering

import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonRepository
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.persistEachIndividuallyAndAggregateResults

class StatusOppdateringRepository(private val database: Database) : BrukernotifikasjonRepository<StatusOppdatering> {

    override suspend fun createInOneBatch(entities: List<StatusOppdatering>): ListPersistActionResult<StatusOppdatering> {
        return database.queryWithExceptionTranslation {
            createStatusOppdateringer(entities)
        }
    }

    override suspend fun createOneByOneToFilterOutTheProblematicEvents(entities: List<StatusOppdatering>): ListPersistActionResult<StatusOppdatering> {
        return database.queryWithExceptionTranslation {
            entities.persistEachIndividuallyAndAggregateResults { entity ->
                createStatusOppdatering(entity)
            }
        }
    }
}