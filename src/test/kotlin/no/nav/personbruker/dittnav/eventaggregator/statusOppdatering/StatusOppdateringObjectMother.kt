package no.nav.personbruker.dittnav.eventaggregator.statusOppdatering

import java.time.LocalDateTime
import java.time.ZoneId

object StatusOppdateringObjectMother {

    fun giveMeStatusOppdatering(eventId: String, fodselsnummer: String): StatusOppdatering {
        val systembruker = "dummySystembruker"
        return giveMeStatusOppdatering(eventId, fodselsnummer, systembruker)
    }

    fun giveMeStatusOppdatering(eventId: String, fodselsnummer: String, systembruker: String): StatusOppdatering {
        return StatusOppdatering(
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