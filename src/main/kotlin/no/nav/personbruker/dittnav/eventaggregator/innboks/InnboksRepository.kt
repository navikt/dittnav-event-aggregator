package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonRepository
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.persistEachIndividuallyAndAggregateResults
import org.slf4j.LoggerFactory

class InnboksRepository(private val database: Database) : BrukernotifikasjonRepository<Innboks> {

    val log = LoggerFactory.getLogger(InnboksRepository::class.java)

    override suspend fun createInOneBatch(entities: List<Innboks>): ListPersistActionResult<Innboks> {
        return database.queryWithExceptionTranslation {
            createInnboksEventer(entities)
        }
    }

    override suspend fun createOneByOneToFilterOutTheProblematicEvents(entities: List<Innboks>): ListPersistActionResult<Innboks> {
        return database.queryWithExceptionTranslation {
            entities.persistEachIndividuallyAndAggregateResults { entity ->
                createInnboks(entity)
            }
        }

    }

}
