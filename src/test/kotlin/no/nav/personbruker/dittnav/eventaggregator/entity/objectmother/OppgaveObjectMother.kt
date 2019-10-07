package no.nav.personbruker.dittnav.eventaggregator.entity.objectmother

import no.nav.personbruker.dittnav.eventaggregator.database.entity.Oppgave
import java.time.LocalDateTime
import java.time.ZoneId

object OppgaveObjectMother {

    fun createOppgave(eventId: String, aktorId: String): Oppgave {
        return Oppgave(
                produsent = "DittNav",
                eventTidspunkt = LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                aktorId = aktorId,
                eventId = eventId,
                dokumentId = "Dok12345",
                tekst = "Dette er en oppgave til brukeren",
                link = "https://nav.no/systemX/",
                sikkerhetsinvaa = 4,
                sistOppdatert = LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                aktiv = true)
    }
}
