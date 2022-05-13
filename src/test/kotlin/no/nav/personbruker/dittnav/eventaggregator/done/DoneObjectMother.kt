package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.Brukernotifikasjon
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object DoneObjectMother {

    fun giveMeDone(eventId: String): Done {
        val systembruker = "dummySystembruker"
        val fodselsnummer = "12345"
        return giveMeDone(eventId, systembruker, fodselsnummer)
    }

    fun giveMeDone(eventId: String, systembruker: String): Done {
        val fodselsnummer = "12345"
        return giveMeDone(eventId, systembruker, fodselsnummer)
    }

    fun giveMeDone(eventId: String, systembruker: String, fodselsnummer: String): Done {
        return Done(
                systembruker = systembruker,
                namespace = "dummyNamespace",
                appnavn = "dummyAppnavn",
                eventId = eventId,
                eventTidspunkt = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS),
                forstBehandlet = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS),
                fodselsnummer = fodselsnummer,
                grupperingsId = "100${eventId}",
                sistBehandlet = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS)
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
