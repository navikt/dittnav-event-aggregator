package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import java.time.LocalDateTime
import java.time.ZoneId

object StatusoppdateringObjectMother {

    fun giveMeStatusoppdatering(eventId: String, fodselsnummer: String): Statusoppdatering {
        val systembruker = "dummySystembruker"
        val link = "https://nav.no/systemX/$eventId"
        return giveMeStatusoppdatering(eventId, fodselsnummer, systembruker, link)
    }

    fun giveMeStatusoppdateringWithLink(link: String): Statusoppdatering {
        return giveMeStatusoppdatering(eventId = "s-1", fodselsnummer = "1234", systembruker = "dummySystemUser", link = link)
    }

    fun giveMeStatusoppdatering(eventId: String, fodselsnummer: String, systembruker: String, link: String): Statusoppdatering {
        return Statusoppdatering(
                systembruker = systembruker,
                eventId = eventId,
                eventTidspunkt = LocalDateTime.now(ZoneId.of("UTC")),
                fodselsnummer = fodselsnummer,
                grupperingsId = "systemA010",
                link = link,
                sikkerhetsnivaa = 4,
                sistOppdatert = LocalDateTime.now(ZoneId.of("UTC")),
                statusGlobal = "SENDT",
                statusIntern = "dummyStatusIntern",
                sakstema = "dummySakstema")
    }

}