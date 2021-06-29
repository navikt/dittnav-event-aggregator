package no.nav.personbruker.dittnav.eventaggregator.beskjed

import java.time.LocalDateTime

data class Beskjed(
        val uid: String,
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
        val synligFremTil: LocalDateTime?,
        val aktiv: Boolean,
        val eksternVarsling: Boolean,
        val prefererteKanaler: List<String> = emptyList()
) {
    constructor(uid: String,
                systembruker: String,
                eventId: String,
                eventTidspunkt: LocalDateTime,
                fodselsnummer: String,
                grupperingsId: String,
                tekst: String,
                link: String,
                sikkerhetsnivaa: Int,
                sistOppdatert: LocalDateTime,
                synligFremTil: LocalDateTime?,
                aktiv: Boolean,
                eksternVarsling: Boolean,
                prefererteKanaler: List<String> = emptyList()
    ) : this(uid,
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
            synligFremTil,
            aktiv,
            eksternVarsling,
            prefererteKanaler
    )

    override fun toString(): String {
        return "Beskjed(" +
                "uid=$uid, " +
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
                "synligFremTil=$synligFremTil, " +
                "aktiv=$aktiv, " +
                "eksternVarsling=$eksternVarsling, " +
                "prefererteKanaler=$prefererteKanaler"
    }
}
