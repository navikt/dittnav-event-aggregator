package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonRepository
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.persistEachIndividuallyAndAggregateResults
import org.slf4j.LoggerFactory

class OppgaveRepository(private val database: Database) : BrukernotifikasjonRepository<Oppgave> {

    private val log = LoggerFactory.getLogger(OppgaveRepository::class.java)

    override suspend fun createInOneBatch(entities: List<Oppgave>): ListPersistActionResult<Oppgave> {
        return database.queryWithExceptionTranslation {
            createOppgaver(entities)
        }
    }

    override suspend fun createOneByOneToFilterOutTheProblematicEvents(entities: List<Oppgave>): ListPersistActionResult<Oppgave> {
        return database.queryWithExceptionTranslation {
            entities.persistEachIndividuallyAndAggregateResults { entity ->
                createOppgave(entity)
            }
        }
    }

    suspend fun getOppgaveWithEksternVarslingForEventIds(eventIds: List<String>): List<Oppgave> {
        return database.queryWithExceptionTranslation {
            getOppgaveWithEksternVarslingForEventIds(eventIds)
        }
    }
}
