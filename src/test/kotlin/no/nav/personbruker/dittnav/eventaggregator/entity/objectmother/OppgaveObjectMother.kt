package no.nav.personbruker.dittnav.eventaggregator.database.entity

import java.time.LocalDateTime
import java.time.ZoneId

object OppgaveObjectMother {

    fun createOppgave(i: Int, aktorId: String): Oppgave {
        return Oppgave(
                id = i,
                produsent = "DittNav",
                eventTidspunkt = LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                aktorId = aktorId,
                eventId = i.toString(),
                dokumentId = "Dok12345",
                tekst = "Dette er en oppgave til brukeren",
                link = "https://nav.no/systemX/$i",
                sikkerhetsinvaa = 4,
                sistOppdatert = LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                aktiv = true
        )
    }
}