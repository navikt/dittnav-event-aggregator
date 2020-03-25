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
        batchOfEntities.forEach { entityToLookFor ->
            val foundMatchingEntity: Brukernotifikasjon? = existingEntitiesInDatabase.find { existingEntity ->
                existingEntity.isRepresentsSameEventAs(entityToLookFor)
            }
            if (foundMatchingEntity != null) {
                groupEventsByType(foundMatchingEntity, entityToLookFor)

            } else {
                notFoundEvents.add(entityToLookFor)
                log.warn("Fant ikke matchende event for done-event med eventId $entityToLookFor")
            }
        }
        log.info(toString())
    }

    private fun groupEventsByType(matchingEntityInTheCache: Brukernotifikasjon, matchedDoneEntity: Done) {
        when (matchingEntityInTheCache.type) {
            EventType.OPPGAVE -> {
                foundOppgave.add(matchedDoneEntity)
            }
            EventType.BESKJED -> {
                foundBeskjed.add(matchedDoneEntity)
            }
            EventType.INNBOKS -> {
                foundInnboks.add(matchedDoneEntity)
            }
            else -> {
                log.warn("Fant ukjent eventtype ved behandling av done-events: $matchingEntityInTheCache")
            }
        }
    }

    fun allFoundEvents(): List<Done> {
        return foundBeskjed + foundInnboks + foundOppgave
    }

    fun totalNumberOfFoundEvents(): Int {
        return foundBeskjed.size + foundInnboks.size + foundOppgave.size
    }

    fun isMoreEventsToProcess(): Boolean {
        return notFoundEvents.isNotEmpty()
    }

    override fun toString(): String {
        return """
            Fant ${foundBeskjed.size} done-eventer med tilhørende eventer i beskjed-tabellen.
            Fant ${foundInnboks.size} done-eventer med tilhørende eventer i innboks-tabellen.
            Fant ${foundOppgave.size} done-eventer med tilhørende eventer i oppgave-tabellen.
            Det er ${notFoundEvents.size} done-eventer det ikke ble funnet et tilhørende event for nå.
        """.trimIndent()
    }

}
