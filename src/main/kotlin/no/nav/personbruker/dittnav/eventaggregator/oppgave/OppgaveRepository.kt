package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonRepository
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistFailureReason
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.countTotalNumberOfEvents
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.countTotalNumberOfEventsByActiveStatus
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import org.slf4j.LoggerFactory

class OppgaveRepository(private val database: Database) : BrukernotifikasjonRepository<Oppgave> {

    private val log = LoggerFactory.getLogger(OppgaveRepository::class.java)

    override suspend fun createInOneBatch(entities: List<Oppgave>) {
        database.queryWithExceptionTranslation {
            createOppgaver(entities)
        }
    }

    override suspend fun createOneByOneToFilterOutTheProblematicEvents(entities: List<Oppgave>) {
        database.queryWithExceptionTranslation {
            entities.forEach { entity ->
                createOppgave(entity).onFailure { reason ->
                    when (reason) {
                        PersistFailureReason.CONFLICTING_KEYS ->
                            log.warn("Hoppet over persistering av Oppgave fordi produsent tidligere har brukt samme eventId: $entity")
                        else ->
                            log.warn("Hoppet over persistering av Oppgave: $entity")
                    }

                }
            }
        }
    }

    override suspend fun getTotalNumberOfEvents(): Long {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEvents(EventType.OPPGAVE)
        }
    }

    override suspend fun getNumberOfActiveEvents(): Long {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEventsByActiveStatus(EventType.OPPGAVE, true)
        }
    }

    override suspend fun getNumberOfInactiveEvents(): Long {
        return database.queryWithExceptionTranslation {
            countTotalNumberOfEventsByActiveStatus(EventType.OPPGAVE, false)
        }
    }

}
