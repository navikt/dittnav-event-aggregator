package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.ConstraintViolationDatabaseException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BeskjedDatabaseService(private val beskjedRepository: BeskjedRepository) {

    private val log: Logger = LoggerFactory.getLogger(BeskjedDatabaseService::class.java)

    suspend fun writeEventsToCache(entities: List<Beskjed>) {
        val allEventsCreated = createBeskjederIEnBatch(entities)

        if (isMaaSkriveEventeneEnEtterEn(allEventsCreated)) {
            beskjedRepository.createOneByOneToFilterOutTheProblematicEvent(entities)
        }
    }

    private suspend fun createBeskjederIEnBatch(entities: List<Beskjed>): Boolean {
        var altOk = true
        try {
            beskjedRepository.createBeskjederIEnBatch(entities)

        } catch (bue: ConstraintViolationDatabaseException) {
            val msg = "Batch-operasjonen mot databasen feilet, prøver heller å skrive eventene en etter en til basen. {}"
            log.warn(msg, bue.toString(), bue)
            altOk = false
        }
        return altOk
    }

    private fun isMaaSkriveEventeneEnEtterEn(allEventsCreated: Boolean) = !allEventsCreated

}
