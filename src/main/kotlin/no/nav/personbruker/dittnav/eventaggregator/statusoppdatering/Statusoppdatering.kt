package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import java.time.LocalDateTime

data class Statusoppdatering(
        val id: Int?,
        val systembruker: String,
        val namespace: String,
        val appnavn: String,
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
            namespace: String,
            appnavn: String,
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
            namespace,
            appnavn,
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
                "namespace=$namespace, " +
                "appnavn=$appnavn, " +
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