package no.nav.personbruker.dittnav.eventaggregator.common.database

import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.AggregatorBatchUpdateException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BrukernotifikasjonPersistingService<T>(private val repository: BrukernotifikasjonRepository<T>) {

    private val log: Logger = LoggerFactory.getLogger(BrukernotifikasjonPersistingService::class.java)

    suspend fun writeEventsToCache(entities: List<T>) {
        val allEventsCreated = createEventsInOneBatch(entities)

        if (isEventsHaveToBeCreatedOneByOne(allEventsCreated)) {
            repository.createOneByOneToFilterOutTheProblematicEvents(entities)
        }
    }

    private suspend fun createEventsInOneBatch(entities: List<T>): Boolean {
        var altOk = true
        try {
            repository.createInOneBatch(entities)

        } catch (bue: AggregatorBatchUpdateException) {
            val msg = "Batch-operasjonen mot databasen feilet, prøver heller å skrive eventene en etter en til basen. {}"
            log.warn(msg, bue.toString(), bue)
            altOk = false
        }
        return altOk
    }

    private fun isEventsHaveToBeCreatedOneByOne(allEventsCreated: Boolean) = !allEventsCreated

}
