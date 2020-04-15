package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.personbruker.dittnav.eventaggregator.common.validation.validateMaxLength
import no.nav.personbruker.dittnav.eventaggregator.common.validation.validateNonNullFieldMaxLength
import no.nav.personbruker.dittnav.eventaggregator.common.validation.validateSikkerhetsnivaa
import java.time.LocalDateTime

data class Beskjed(
        val uid: String,
        val id: Int?,
        val produsent: String,
        val eventId: String,
        val eventTidspunkt: LocalDateTime,
        val fodselsnummer: String,
        val grupperingsId: String,
        val tekst: String,
        val link: String,
        val sikkerhetsnivaa: Int,
        val sistOppdatert: LocalDateTime,
        val synligFremTil: LocalDateTime?,
        val aktiv: Boolean
) {
    constructor(uid: String,
                produsent: String,
                eventId: String,
                eventTidspunkt: LocalDateTime,
                fodselsnummer: String,
                grupperingsId: String,
                tekst: String,
                link: String,
                sikkerhetsnivaa: Int,
                sistOppdatert: LocalDateTime,
                synligFremTil: LocalDateTime?,
                aktiv: Boolean
    ) : this(uid,
            null,
            produsent,
            eventId,
            eventTidspunkt,
            fodselsnummer,
            grupperingsId,
            tekst,
            link,
            sikkerhetsnivaa,
            sistOppdatert,
            synligFremTil,
            aktiv
    ) {
        validateNonNullFieldMaxLength(produsent, "produsent", 100)
        validateNonNullFieldMaxLength(eventId, "eventId", 50)
        validateNonNullFieldMaxLength(fodselsnummer, "fodselsnummer", 11)
        validateNonNullFieldMaxLength(grupperingsId, "grupperingsId", 100)
        validateNonNullFieldMaxLength(tekst, "tekst", 500)
        validateMaxLength(link, "link", 200)
        validateSikkerhetsnivaa(sikkerhetsnivaa)
        validateNonNullFieldMaxLength(uid, "uid", 100)
    }

    override fun toString(): String {
        return "Beskjed(" +
                "uid=$uid, " +
                "id=$id, " +
                "produsent=$produsent, " +
                "eventId=$eventId, " +
                "eventTidspunkt=$eventTidspunkt, " +
                "fodselsnummer=***, " +
                "grupperingsId=$grupperingsId, " +
                "tekst=***, " +
                "link=***, " +
                "sikkerhetsnivaa=$sikkerhetsnivaa, " +
                "sistOppdatert=$sistOppdatert, " +
                "synligFremTil=$synligFremTil, " +
                "aktiv=$aktiv"
    }

}
