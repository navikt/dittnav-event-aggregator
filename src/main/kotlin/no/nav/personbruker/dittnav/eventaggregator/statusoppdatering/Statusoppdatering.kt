package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import no.nav.brukernotifikasjon.schemas.builders.domain.Eventtype
import no.nav.brukernotifikasjon.schemas.builders.util.ValidationUtil
import no.nav.brukernotifikasjon.schemas.builders.util.ValidationUtil.MAX_LENGTH_SAKSTEMA
import no.nav.brukernotifikasjon.schemas.builders.util.ValidationUtil.MAX_LENGTH_STATUSINTERN
import java.time.LocalDateTime

data class Statusoppdatering(
        val id: Int?,
        val systembruker: String,
        val eventId: String,
        val eventTidspunkt: LocalDateTime,
        val fodselsnummer: String,
        val grupperingsId: String,
        val link: String,
        val sikkerhetsnivaa: Int,
        val sistOppdatert: LocalDateTime,
        val statusGlobal: String,
        val statusIntern: String?,
        val sakstema: String
) {
    constructor(
            systembruker: String,
            eventId: String,
            eventTidspunkt: LocalDateTime,
            fodselsnummer: String,
            grupperingsId: String,
            link: String,
            sikkerhetsnivaa: Int,
            sistOppdatert: LocalDateTime,
            statusGlobal: String,
            statusIntern: String?,
            sakstema: String
    ) : this(null,
            systembruker,
            eventId,
            eventTidspunkt,
            fodselsnummer,
            grupperingsId,
            link,
            sikkerhetsnivaa,
            sistOppdatert,
            statusGlobal,
            statusIntern,
            sakstema

    )

    override fun toString(): String {
        return "Statusoppdatering(" +
                "id=$id, " +
                "systembruker=***, " +
                "eventId=$eventId, " +
                "eventTidspunkt=$eventTidspunkt, " +
                "fodselsnummer=***, " +
                "grupperingsId=$grupperingsId, " +
                "link=***, " +
                "sikkerhetsnivaa=$sikkerhetsnivaa, " +
                "sistOppdatert=$sistOppdatert, " +
                "statusGlobal=$statusGlobal, " +
                "statusIntern=$statusIntern, " +
                "sakstema=$sakstema, "
    }

}