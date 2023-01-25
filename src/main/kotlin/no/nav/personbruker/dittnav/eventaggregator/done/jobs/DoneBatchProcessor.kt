package no.nav.personbruker.dittnav.eventaggregator.done.jobs

import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.done.Done
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselHeader
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType.*

class DoneBatchProcessor(private val existingEntitiesInDatabase: List<VarselHeader>) {

    private val maxPossibleRequiredCapacity = existingEntitiesInDatabase.size
    val foundBeskjed = ArrayList<Done>(maxPossibleRequiredCapacity)
    val foundOppgave = ArrayList<Done>(maxPossibleRequiredCapacity)
    val foundInnboks = ArrayList<Done>(maxPossibleRequiredCapacity)
    val notFoundEvents = ArrayList<Done>(maxPossibleRequiredCapacity)

    val allFoundEvents: List<Done> get() = foundBeskjed + foundOppgave + foundInnboks

    val allFoundEventsByType: List<Pair<EventType, Done>> get() =
        foundBeskjed.map { EventType.BESKJED_INTERN to it } +
        foundOppgave.map { EventType.OPPGAVE_INTERN to it } +
        foundInnboks.map { EventType.INNBOKS_INTERN to it }

    fun process(batchOfEntities: List<Done>) {
        batchOfEntities.forEach { entityToLookFor ->
            val foundMatchingEntity = existingEntitiesInDatabase.find { existingEntity ->
                existingEntity.representsSameVarsel(entityToLookFor)
            }

            if (foundMatchingEntity != null) {
                groupVarslerByType(foundMatchingEntity, entityToLookFor)
            } else {
                notFoundEvents.add(entityToLookFor)
            }
        }
    }

    private fun groupVarslerByType(matchingEntityInTheCache: VarselHeader, matchedDoneEntity: Done) {
        when (matchingEntityInTheCache.type) {
            OPPGAVE -> {
                foundOppgave.add(matchedDoneEntity)
            }
            BESKJED -> {
                foundBeskjed.add(matchedDoneEntity)
            }
            INNBOKS -> {
                foundInnboks.add(matchedDoneEntity)
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

    private fun VarselHeader.representsSameVarsel(done: Done) = eventId == done.eventId

}
