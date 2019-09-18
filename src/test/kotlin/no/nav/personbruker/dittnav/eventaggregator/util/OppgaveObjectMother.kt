package no.nav.personbruker.dittnav.eventaggregator.util

import no.nav.personbruker.dittnav.eventaggregator.database.entity.Oppgave
import java.time.LocalDateTime
import java.time.ZoneId

object OppgaveObjectMother {

    fun createOppgave(i: Int): Oppgave {
        return Oppgave(
                id = i,
                produsent = "DittNav",
                eventTidspunkt = LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                aktoerId = "12345",
                eventId = i.toString(),
                dokumentId = "Dok12345",
                tekst = "Dette er en oppgave til brukeren",
                link = "https://nav.no/systemX/$i",
                sikkerhetsinvaa = 4
        )
    }

    fun createOppgave(i: Int, aktoerId: String): Oppgave {
        return Oppgave(
                id = i,
                produsent = "DittNav",
                eventTidspunkt = LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                aktoerId = aktoerId,
                eventId = i.toString(),
                dokumentId = "Dok12345",
                tekst = "Dette er en oppgave til brukeren",
                link = "https://nav.no/systemX/$i",
                sikkerhetsinvaa = 4
        )
    }
}