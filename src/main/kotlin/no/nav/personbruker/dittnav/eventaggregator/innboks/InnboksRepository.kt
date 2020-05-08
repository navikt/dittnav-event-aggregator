package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonRepository
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistFailureReason
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

}
