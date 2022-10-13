package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.Brukernotifikasjon
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DoneBatchProcessor(private val existingEntitiesInDatabase: List<Brukernotifikasjon>) {

    private val log: Logger = LoggerFactory.getLogger(DoneBatchProcessor::class.java)

    private val maxPossibleRequiredCapacity = existingEntitiesInDatabase.size
    val foundBeskjed = ArrayList<Done>(maxPossibleRequiredCapacity)
    val foundOppgave = ArrayList<Done>(maxPossibleRequiredCapacity)
    val foundInnboks = ArrayList<Done>(maxPossibleRequiredCapacity)
    val allFoundEvents = ArrayList<Done>(maxPossibleRequiredCapacity)
    val notFoundEvents = ArrayList<Done>(maxPossibleRequiredCapacity)

    fun process(batchOfEntities: List<Done>) {
        batchOfEntities.forEach { entityToLookFor ->
            val foundMatchingEntity: Brukernotifikasjon? = existingEntitiesInDatabase.find { existingEntity ->
                existingEntity.isRepresentsSameEventAs(entityToLookFor)
            }
            if (foundMatchingEntity != null) {
                groupEventsByType(foundMatchingEntity, entityToLookFor)

            } else {
                notFoundEvents.add(entityToLookFor)
            }
        }
    }

    private fun groupEventsByType(matchingEntityInTheCache: Brukernotifikasjon, matchedDoneEntity: Done) {
        allFoundEvents.add(matchedDoneEntity)
        when (matchingEntityInTheCache.type) {
            EventType.OPPGAVE_INTERN -> {
                foundOppgave.add(matchedDoneEntity)
            }
            EventType.BESKJED_INTERN -> {
                foundBeskjed.add(matchedDoneEntity)
            }
            EventType.INNBOKS_INTERN -> {
                foundInnboks.add(matchedDoneEntity)
            }
            else -> {
                log.warn("Fant ukjent eventtype ved behandling av done-events: $matchingEntityInTheCache")
            }
        }
    }

    override fun toString(): String {
        val antallDoneEventer = allFoundEvents.size + notFoundEvents.size
        val antallBrukernotifikasjoner = existingEntitiesInDatabase.size
        return """
            Prosesserte $antallDoneEventer done-eventer, opp mot $antallBrukernotifikasjoner brukernotifikasjoner:
            Fant ${foundBeskjed.size} done-eventer med tilhørende eventer i beskjed-tabellen.
            Fant ${foundInnboks.size} done-eventer med tilhørende eventer i innboks-tabellen.
            Fant ${foundOppgave.size} done-eventer med tilhørende eventer i oppgave-tabellen.
            Det er ${notFoundEvents.size} done-eventer det ikke ble funnet et tilhørende event for nå.
        """.trimIndent()
    }

}
