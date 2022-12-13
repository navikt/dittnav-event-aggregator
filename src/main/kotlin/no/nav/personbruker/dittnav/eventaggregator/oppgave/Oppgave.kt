package no.nav.personbruker.dittnav.eventaggregator.oppgave

import java.time.LocalDateTime

data class Oppgave(
        val eventId: String,
        val systembruker: String,
        val namespace: String,
        val appnavn: String,
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
        val synligFremTil: LocalDateTime?,
        val smsVarslingstekst: String? = null,
        val epostVarslingstekst: String? = null,
        val epostVarslingstittel: String? = null
)
