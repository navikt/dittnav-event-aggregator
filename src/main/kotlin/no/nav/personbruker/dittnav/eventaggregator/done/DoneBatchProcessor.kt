package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.Brukernotifikasjon
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.EventType.*
import org.slf4j.LoggerFactory

object DoneBatchProcessor {

    private val log = LoggerFactory.getLogger(DoneBatchProcessor::class.java)

    fun process(doneEvents: List<Done>, existingEvents: List<Brukernotifikasjon>): CachedDoneProcessingResult {

        val eventsByType = existingEvents.groupBy { event -> event.type }

        logInvalidEventsTypes(eventsByType)

        val matchingBeskjed = findMatchingDoneEventsOfType(doneEvents, eventsByType, BESKJED)
        val matchingOppgave = findMatchingDoneEventsOfType(doneEvents, eventsByType, OPPGAVE)
        val matchingInnboks = findMatchingDoneEventsOfType(doneEvents, eventsByType, INNBOKS)

        val matchingNone = doneEvents
                .filter { done -> !matchingBeskjed.contains(done) }
                .filter { done -> !matchingOppgave.contains(done) }
                .filter { done -> !matchingInnboks.contains(done) }

        return CachedDoneProcessingResult(
                eventsMatchingBeskjed = matchingBeskjed,
                eventsMatchingOppgave = matchingOppgave,
                eventsMatchingInnboks = matchingInnboks,
                eventsMatchingNone = matchingNone
        )
    }

    private fun findMatchingDoneEventsOfType(doneEvents: List<Done>,
                                             existingEventsByType: Map<EventType, List<Brukernotifikasjon>>,
                                             type: EventType): List<Done> {

        val matchesFoundForType = existingEventsByType[type]

        return if (matchesFoundForType == null) {
            emptyList()
        } else {
            findMatchingDoneEvents(doneEvents, matchesFoundForType)
        }
    }

    private fun logInvalidEventsTypes(existingEventsByType: Map<EventType, List<Brukernotifikasjon>>) {
        val knownTypes = listOf( BESKJED, OPPGAVE, INNBOKS)
        val unknownTypes = existingEventsByType.keys.filter { key -> !knownTypes.contains(key) }

        if (!unknownTypes.isEmpty()) {
            log.warn("Fant ukjent(e) eventtype(r) ved behandling av done-events: $unknownTypes")
        }
    }

    private fun findMatchingDoneEvents(doneEvents: List<Done>, events: List<Brukernotifikasjon>): List<Done> {
        return doneEvents.filter { done ->
            events.any { event ->
                event.representsSameEventAs(done)
            }
        }
    }

}
