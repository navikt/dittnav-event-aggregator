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

    fun process(batchOfEvents: List<Done>) {
        batchOfEvents.forEach { doneEntity ->
            val foundEvent: Brukernotifikasjon? = existingEntitiesInDatabase.find { existingEntity ->
                isEventInCache(existingEntity, doneEntity)
            }
            if (foundEvent != null) {
                log.info("Fant matchende event for Done-event med eventId: ${foundEvent.eventId}, produsent: ${foundEvent.produsent}, type: ${foundEvent.type}")
                groupEventsByType(foundEvent, doneEntity)

            } else {
                notFoundEvents.add(doneEntity)
                log.info("Fant ikke matchende event for done-event med eventId ${doneEntity.eventId}, produsent: ${doneEntity.produsent}, eventTidspunkt: ${doneEntity.eventTidspunkt}. Skrev done-event til cache")
            }
        }
    }

    private fun isEventInCache(brukernotifikasjon: Brukernotifikasjon, done: Done): Boolean {
        return (brukernotifikasjon.eventId == done.eventId &&
                brukernotifikasjon.produsent == done.produsent &&
                brukernotifikasjon.fodselsnummer == done.fodselsnummer)
    }

    fun groupEventsByType(event: Brukernotifikasjon, done: Done) {
        when (event.type) {
            EventType.OPPGAVE -> {
                foundOppgave.add(done)
            }
            EventType.BESKJED -> {
                foundBeskjed.add(done)
            }
            EventType.INNBOKS -> {
                foundInnboks.add(done)
            }
            else -> {
                log.warn("Fant ukjent eventtype ved behandling av done-events: $event")
            }
        }
    }

}
