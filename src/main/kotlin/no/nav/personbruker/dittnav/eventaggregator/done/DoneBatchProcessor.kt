package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.Brukernotifikasjon
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DoneBatchProcessor(private val existingEntitiesInDatabase: List<Brukernotifikasjon>) {

    private val log: Logger = LoggerFactory.getLogger(DoneBatchProcessor::class.java)

    val foundBeskjed = mutableListOf<Done>()
    val foundOppgave = mutableListOf<Done>()
    val foundInnboks = mutableListOf<Done>()
    val notFoundEvents = mutableListOf<Done>()

    fun process(batchOfEntities: List<Done>) {
        batchOfEntities.forEach { entityToLookForInTheCache ->
            val foundEntity: Brukernotifikasjon? = existingEntitiesInDatabase.find { existingEntity ->
                isAssociatedEntities(existingEntity, entityToLookForInTheCache)
            }
            if (foundEntity != null) {
                log.info("Fant matchende event for Done-eventet: $foundEntity")
                groupEventsByType(foundEntity, entityToLookForInTheCache)

            } else {
                notFoundEvents.add(entityToLookForInTheCache)
                log.info("Fant ikke matchende event for done-event med eventId $entityToLookForInTheCache")
            }
        }
    }

    private fun isAssociatedEntities(entityInTheCache: Brukernotifikasjon, entityToLookForInTheCache: Done): Boolean {
        return (entityInTheCache.eventId == entityToLookForInTheCache.eventId &&
                entityInTheCache.produsent == entityToLookForInTheCache.produsent &&
                entityInTheCache.fodselsnummer == entityToLookForInTheCache.fodselsnummer)
    }

    private fun groupEventsByType(entityInTheCache: Brukernotifikasjon, entityFound: Done) {
        when (entityInTheCache.type) {
            EventType.OPPGAVE -> {
                foundOppgave.add(entityFound)
            }
            EventType.BESKJED -> {
                foundBeskjed.add(entityFound)
            }
            EventType.INNBOKS -> {
                foundInnboks.add(entityFound)
            }
            else -> {
                log.warn("Fant ukjent eventtype ved behandling av done-events: $entityInTheCache")
            }
        }
    }

}
