package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.Brukernotifikasjon
import java.time.LocalDateTime
import java.time.ZoneId

object DoneObjectMother {

    fun giveMeDone(eventId: String): Done {
        val systembruker = "dummyProducer"
        val fodselsnummer = "12345"
        return giveMeDone(eventId, systembruker, fodselsnummer)
    }

    fun giveMeDone(eventId: String, systembruker: String, fodselsnummer: String): Done {
        return Done(
                systembruker,
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
                    brukernotifikasjon.systembruker,
                    brukernotifikasjon.fodselsnummer
            )
            doneEvents.add(associatedDoneEvent)
        }
        return doneEvents
    }

    fun giveMeMatchingDoneEvent(brukernotifikasjonToMatch: Brukernotifikasjon): Done {
        return giveMeDone(
                brukernotifikasjonToMatch.eventId,
                brukernotifikasjonToMatch.systembruker,
                brukernotifikasjonToMatch.fodselsnummer
        )
    }

}
