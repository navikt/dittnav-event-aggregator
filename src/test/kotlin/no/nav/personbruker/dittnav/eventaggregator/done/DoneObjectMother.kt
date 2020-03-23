package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.Brukernotifikasjon
import no.nav.personbruker.dittnav.eventaggregator.innboks.Innboks
import no.nav.personbruker.dittnav.eventaggregator.oppgave.Oppgave
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

    fun giveMeMatchingDoneEvent(beskjedToMatch: Beskjed): Done {
        return createDone(
                beskjedToMatch.eventId,
                beskjedToMatch.produsent,
                beskjedToMatch.fodselsnummer
        )
    }

    fun giveMeMatchingDoneEvent(innboksToMatch: Innboks): Done {
        return createDone(
                innboksToMatch.eventId,
                innboksToMatch.produsent,
                innboksToMatch.fodselsnummer
        )
    }

    fun giveMeMatchingDoneEvent(oppgaveToMatch: Oppgave): Done {
        return createDone(
                oppgaveToMatch.eventId,
                oppgaveToMatch.produsent,
                oppgaveToMatch.fodselsnummer
        )
    }

}
