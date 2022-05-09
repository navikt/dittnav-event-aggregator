package no.nav.personbruker.dittnav.eventaggregator.oppgave

import java.time.LocalDateTime

data class Oppgave(
        val id: Int?,
        val systembruker: String,
        val namespace: String,
        val appnavn: String,
        val eventId: String,
        val eventTidspunkt: LocalDateTime,
        val forstBehandlet: LocalDateTime,
        val fodselsnummer: String,
        val grupperingsId: String,
        val tekst: String,
        val link: String,
        val sikkerhetsnivaa: Int,
        val sistOppdatert: LocalDateTime,
        val aktiv: Boolean,
        val eksternVarsling: Boolean,
        val prefererteKanaler: List<String> = emptyList(),
        val synligFremTil: LocalDateTime? = null
) {
    constructor(
            systembruker: String,
            namespace: String,
            appnavn: String,
            eventId: String,
            eventTidspunkt: LocalDateTime,
            forstBehandlet: LocalDateTime,
            fodselsnummer: String,
            grupperingsId: String,
            tekst: String,
            link: String,
            sikkerhetsnivaa: Int,
            sistOppdatert: LocalDateTime,
            aktiv: Boolean,
            eksternVarsling: Boolean,
            prefererteKanaler: List<String> = emptyList(),
            synligFremTil: LocalDateTime? = null
    ) : this(null,
            systembruker,
            namespace,
            appnavn,
            eventId,
            eventTidspunkt,
            forstBehandlet,
            fodselsnummer,
            grupperingsId,
            tekst,
            link,
            sikkerhetsnivaa,
            sistOppdatert,
            aktiv,
            eksternVarsling,
            prefererteKanaler,
            synligFremTil
    )

    override fun toString(): String {
        return "Oppgave(" +
                "id=$id, " +
                "systembruker=***, " +
                "namespace=$namespace, " +
                "appnavn=$appnavn, " +
                "eventId=$eventId, " +
                "eventTidspunkt=$eventTidspunkt, " +
                "forstBehandlet=$forstBehandlet, " +
                "fodselsnummer=***, " +
                "grupperingsId=$grupperingsId, " +
                "tekst=***, " +
                "link=***, " +
                "sikkerhetsnivaa=$sikkerhetsnivaa, " +
                "sistOppdatert=$sistOppdatert, " +
                "aktiv=$aktiv, " +
                "eksternVarsling=$eksternVarsling, " +
                "prefererteKanaler=$prefererteKanaler, " +
                "synligFremTil=$synligFremTil"
    }
}
