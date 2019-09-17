package no.nav.personbruker.dittnav.eventaggregator.database.entity

import java.time.LocalDateTime
import java.time.ZoneId

object InformasjonObjectMother {

    fun createInformasjon(i: Int): Informasjon {
        return Informasjon(
                id = i,
                produsent = "DittNAV",
                eventTidspunkt = LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                aktorId = "12345",
                eventId = i.toString(),
                dokumentId = "100$i",
                tekst = "Dette er informasjon til brukeren",
                link = "https://nav.no/systemX/$i",
                sistOppdatert = LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                sikkerhetsnivaa = 4,
                aktiv = true)
    }

}
