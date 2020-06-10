package no.nav.personbruker.dittnav.eventaggregator.common.database

import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.AggregatorBatchUpdateException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BrukernotifikasjonPersistingService<T>(private val repository: BrukernotifikasjonRepository<T>) {

    private val log: Logger = LoggerFactory.getLogger(BrukernotifikasjonPersistingService::class.java)

    suspend fun writeEventsToCache(entities: List<T>): ListPersistActionResult<T> {

        return try {
            repository.createInOneBatch(entities)
        } catch (bue: AggregatorBatchUpdateException) {
            val msg = "Batch-operasjonen mot databasen feilet, prøver heller å skrive eventene en etter en til basen. {}"
            log.warn(msg, bue.toString(), bue)
            repository.createOneByOneToFilterOutTheProblematicEvents(entities)
        }
    }
}
