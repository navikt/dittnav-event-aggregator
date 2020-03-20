package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.Brukernotifikasjon
import java.time.LocalDateTime
import java.time.ZoneId

object DoneObjectMother {

    fun createDone(eventId: String): Done {
        val produsent = "DittNAV"
        val fodselsnummer = "12345"
        return createDone(eventId, produsent, fodselsnummer)
    }

    fun createDone(eventId: String, produsent: String, fodselsnummer: String): Done {
        return Done(
                produsent,
                eventId,
                LocalDateTime.now(ZoneId.of("UTC")),
                fodselsnummer,
                "100${eventId}"
        )
    }

    fun giveMeOneDoneEventForEach(brukernotifikasjoner: List<Brukernotifikasjon>): MutableList<Done> {
        val doneEvents = mutableListOf<Done>()
        brukernotifikasjoner.forEach { brukernotifikasjon ->
            val associatedDoneEvent = createDone(
                    brukernotifikasjon.eventId,
                    brukernotifikasjon.produsent,
                    brukernotifikasjon.fodselsnummer
            )
            doneEvents.add(associatedDoneEvent)
        }
        return doneEvents
    }
}
