package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.brukernotifikasjon.schemas.builders.domain.Eventtype
import no.nav.brukernotifikasjon.schemas.builders.util.ValidationUtil
import java.time.LocalDateTime

data class Innboks(
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
        ValidationUtil.validateNonNullFieldMaxLength(systembruker, "systembruker", ValidationUtil.MAX_LENGTH_SYSTEMBRUKER)
        ValidationUtil.validateNonNullFieldMaxLength(eventId, "eventId", ValidationUtil.MAX_LENGTH_EVENTID)
        ValidationUtil.validateNonNullFieldMaxLength(fodselsnummer, "fodselsnummer", ValidationUtil.MAX_LENGTH_FODSELSNUMMER)
        ValidationUtil.validateNonNullFieldMaxLength(grupperingsId, "grupperingsId", ValidationUtil.MAX_LENGTH_GRUPPERINGSID)
        ValidationUtil.validateNonNullFieldMaxLength(tekst, "tekst", ValidationUtil.MAX_LENGTH_TEXT_INNBOKS)
        ValidationUtil.validateLinkAndConvertToString(ValidationUtil.validateLinkAndConvertToURL(link), "link", ValidationUtil.MAX_LENGTH_LINK, ValidationUtil.isLinkRequired(Eventtype.INNBOKS))
        ValidationUtil.validateSikkerhetsnivaa(sikkerhetsnivaa)
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
