package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.Brukernotifikasjon
import java.time.LocalDateTime
import java.time.ZoneId

object DoneObjectMother {

    fun giveMeDone(eventId: String): Done {
        val produsent = "dummyProducer"
        val fodselsnummer = "12345"
        return giveMeDone(eventId, produsent, fodselsnummer)
    }

    fun giveMeDone(eventId: String, produsent: String, fodselsnummer: String): Done {
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
            val associatedDoneEvent = giveMeDone(
                    brukernotifikasjon.eventId,
                    brukernotifikasjon.produsent,
                    brukernotifikasjon.fodselsnummer
            )
            doneEvents.add(associatedDoneEvent)
        }
        return doneEvents
    }

    fun giveMeMatchingDoneEvent(brukernotifikasjonToMatch: Brukernotifikasjon): Done {
        return giveMeDone(
                brukernotifikasjonToMatch.eventId,
                brukernotifikasjonToMatch.produsent,
                brukernotifikasjonToMatch.fodselsnummer
        )
    }

}
