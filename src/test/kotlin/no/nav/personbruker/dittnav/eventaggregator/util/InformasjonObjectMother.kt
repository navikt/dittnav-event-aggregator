package no.nav.personbruker.dittnav.eventaggregator.util

import no.nav.personbruker.dittnav.eventaggregator.database.entity.Informasjon
import java.time.LocalDateTime
import java.util.*

object InformasjonObjectMother {

    fun createInformasjon(i: Int): Informasjon {
        return Informasjon(
                id=i,
                produsent = "DittNAV",
                eventTidspunkt = LocalDateTime.now(),
                aktorId = "12345",
                eventId = i.toString(),
                dokumentId = "100$i",
                tekst = "Dette er informasjon til brukeren",
                link = "https://nav.no/systemX/$i",
                sistOppdatert = LocalDateTime.now(),
                sikkerhetsnivaa = 4,
                aktiv = true)
    }

}