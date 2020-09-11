package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import java.time.LocalDateTime
import java.time.ZoneId

object StatusoppdateringObjectMother {

    fun giveMeStatusoppdatering(eventId: String, fodselsnummer: String): Statusoppdatering {
        val systembruker = "dummySystembruker"
        return giveMeStatusoppdatering(eventId, fodselsnummer, systembruker)
    }

    fun giveMeStatusoppdatering(eventId: String, fodselsnummer: String, systembruker: String): Statusoppdatering {
        return Statusoppdatering(
                systembruker = systembruker,
                eventId = eventId,
                eventTidspunkt = LocalDateTime.now(ZoneId.of("UTC")),
                fodselsnummer = fodselsnummer,
                grupperingsId = "systemA010",
                link = "https://nav.no/systemX/$eventId",
                sikkerhetsnivaa = 4,
                sistOppdatert = LocalDateTime.now(ZoneId.of("UTC")),
                statusGlobal = "SENDT",
                statusIntern = "dummyStatusIntern",
                sakstema = "dummySakstema")
    }

}