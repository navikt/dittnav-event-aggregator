package no.nav.personbruker.dittnav.eventaggregator.informasjon

import java.time.LocalDateTime
import java.time.ZoneId

object InformasjonObjectMother {

    fun createInformasjon(eventId: String, aktorId: String): Informasjon {
        return Informasjon(
                produsent = "DittNav",
                eventTidspunkt = LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                aktorId = aktorId,
                eventId = eventId,
                dokumentId = "100$aktorId",
                tekst = "Dette er informasjon til brukeren",
                link = "https://nav.no/systemX/$aktorId",
                sistOppdatert = LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                sikkerhetsnivaa = 4,
                aktiv = true)
    }
}
