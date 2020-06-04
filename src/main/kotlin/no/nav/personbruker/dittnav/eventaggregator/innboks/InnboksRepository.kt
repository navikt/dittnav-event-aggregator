package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonRepository
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistFailureReason
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.countTotalNumberOfEvents
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.countTotalNumberOfEventsByActiveStatus
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import org.slf4j.LoggerFactory

class InnboksRepository(private val database: Database) : BrukernotifikasjonRepository<Innboks> {

    val log = LoggerFactory.getLogger(InnboksRepository::class.java)

    override suspend fun createInOneBatch(entities: List<Innboks>) {
        database.queryWithExceptionTranslation {
            createInnboksEventer(entities)
        }
    }

    override suspend fun createOneByOneToFilterOutTheProblematicEvents(entities: List<Innboks>) {
        database.queryWithExceptionTranslation {
            entities.forEach { entity ->
                createInnboks(entity).onFailure { reason ->
                    when (reason) {
                        PersistFailureReason.CONFLICTING_KEYS ->
                            log.warn("Hoppet over persistering av Innboks fordi produsent tidligere har brukt samme eventId: $entity")
                        else ->
                            log.warn("Hoppet over persistering av Innboks: $entity")
                    }

                }
            }
        }

    }

    override suspend fun getTotalNumberOfEvents(): Long {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEvents(EventType.INNBOKS)
        }
    }

    override suspend fun getNumberOfActiveEvents(): Long {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEventsByActiveStatus(EventType.INNBOKS, true)
        }
    }

    override suspend fun getNumberOfInactiveEvents(): Long {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEventsByActiveStatus(EventType.INNBOKS, false)
        }
    }

}
