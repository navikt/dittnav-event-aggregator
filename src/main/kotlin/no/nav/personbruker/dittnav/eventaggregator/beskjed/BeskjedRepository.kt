package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistFailureReason
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BeskjedRepository(private val database: Database) {

    private val log: Logger = LoggerFactory.getLogger(BeskjedRepository::class.java)

    suspend fun createBeskjederIEnBatch(entities: List<Beskjed>) {
        database.queryWithExceptionTranslation {
            createBeskjeder(entities)
        }
    }

    suspend fun createOneByOneToFilterOutTheProblematicEvent(entities: List<Beskjed>) {
        database.queryWithExceptionTranslation {
            entities.forEach { entity ->
                createBeskjed(entity).onFailure { reason ->
                    when (reason) {
                        PersistFailureReason.CONFLICTING_KEYS ->
                            log.warn("Hoppet over persistering av Beskjed fordi produsent tidligere har brukt samme eventId: $entity")
                        else ->
                            log.warn("Hoppet over persistering av Beskjed: $entity")
                    }

                }
            }
        }
    }

}
