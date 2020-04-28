package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.personbruker.dittnav.eventaggregator.common.validation.validateMaxLength
import no.nav.personbruker.dittnav.eventaggregator.common.validation.validateNonNullFieldMaxLength
import no.nav.personbruker.dittnav.eventaggregator.common.validation.validateSikkerhetsnivaa
import java.time.LocalDateTime

data class Innboks (
        val id: Int?,
        val systembruker: String,
        val eventId: String,
        val eventTidspunkt: LocalDateTime,
        val fodselsnummer: String,
        val grupperingsId: String,
        val tekst: String,
        val link: String,
        val sikkerhetsnivaa: Int,
        val sistOppdatert: LocalDateTime,
        val aktiv: Boolean
) {
    constructor(
            systembruker: String,
            eventId: String,
            eventTidspunkt: LocalDateTime,
            fodselsnummer: String,
            grupperingsId: String,
            tekst: String,
            link: String,
            sikkerhetsnivaa: Int,
            sistOppdatert: LocalDateTime,
            aktiv: Boolean
    ) : this(
            null,
            systembruker,
            eventId,
            eventTidspunkt,
            fodselsnummer,
            grupperingsId,
            tekst,
            link,
            sikkerhetsnivaa,
            sistOppdatert,
            aktiv
    ) {
        validateNonNullFieldMaxLength(systembruker, "systembruker", 100)
        validateNonNullFieldMaxLength(eventId, "eventId", 50)
        validateNonNullFieldMaxLength(fodselsnummer, "fodselsnummer", 11)
        validateNonNullFieldMaxLength(grupperingsId, "grupperingsId", 100)
        validateNonNullFieldMaxLength(tekst, "tekst", 500)
        validateMaxLength(link, "link", 200)
        validateSikkerhetsnivaa(sikkerhetsnivaa)
    }

    override fun toString(): String {
        return "Innboks(" +
                "id=$id, " +
                "systembruker=***, " +
                "eventId=$eventId, " +
                "eventTidspunkt=$eventTidspunkt, " +
                "fodselsnummer=***, " +
                "grupperingsId=$grupperingsId, " +
                "tekst=***, " +
                "link=***, " +
                "sikkerhetsnivaa=$sikkerhetsnivaa, " +
                "sistOppdatert=$sistOppdatert, " +
                "aktiv=$aktiv"
    }

}
