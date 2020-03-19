package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistFailureReason
import org.slf4j.LoggerFactory

class InnboksRepository(private val database: Database) {

    val log = LoggerFactory.getLogger(InnboksRepository::class.java)

    suspend fun writeEventsToCache(entities: List<Innboks>) {
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

}
