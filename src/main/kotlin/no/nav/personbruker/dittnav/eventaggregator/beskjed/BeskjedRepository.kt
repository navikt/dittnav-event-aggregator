package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonRepository
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.countTotalNumberOfEvents
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.countTotalNumberOfEventsByActiveStatus
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.persistEachIndividuallyAndAggregateResults
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BeskjedRepository(private val database: Database) : BrukernotifikasjonRepository<Beskjed> {

    private val log: Logger = LoggerFactory.getLogger(BeskjedRepository::class.java)

    override suspend fun createInOneBatch(entities: List<Beskjed>): ListPersistActionResult<Beskjed> {
        return database.queryWithExceptionTranslation {
            createBeskjeder(entities)
        }
    }

    override suspend fun createOneByOneToFilterOutTheProblematicEvents(entities: List<Beskjed>): ListPersistActionResult<Beskjed> {
        return database.queryWithExceptionTranslation {
            entities.persistEachIndividuallyAndAggregateResults { entity ->
                createBeskjed(entity)
            }
        }
    }

    override suspend fun getTotalNumberOfEvents(): Long {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEvents(EventType.BESKJED)
        }
    }

    override suspend fun getNumberOfActiveEvents(): Long {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEventsByActiveStatus(EventType.BESKJED, true)
        }
    }

    override suspend fun getNumberOfInactiveEvents(): Long {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEventsByActiveStatus(EventType.BESKJED, false)
        }
    }

}
