package no.nav.personbruker.dittnav.eventaggregator.entity.objectmother

import no.nav.personbruker.dittnav.eventaggregator.database.entity.Informasjon
import java.time.LocalDateTime
import java.time.ZoneId

object InformasjonObjectMother {

    fun createInformasjon(id : Int, aktorId: String): Informasjon {
        return Informasjon(
                id = id,
                produsent = "DittNAV",
                eventTidspunkt = LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                aktorId = aktorId,
                eventId = "$id",
                dokumentId = "100$aktorId",
                tekst = "Dette er informasjon til brukeren",
                link = "https://nav.no/systemX/$aktorId",
                sistOppdatert = LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                sikkerhetsnivaa = 4,
                aktiv = true)
    }

}
