package no.nav.personbruker.dittnav.eventaggregator.beskjed

import java.time.LocalDateTime

data class Beskjed(
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
        val synligFremTil: LocalDateTime?,
        val aktiv: Boolean,
        val eksternVarsling: Boolean,
        val prefererteKanaler: List<String> = emptyList(),
        val smsVarslingstekst: String? = null,
        val epostVarslingstekst: String? = null,
        val epostVarslingstittel: String? = null,
        val fristUtløpt: Boolean? = null
)
