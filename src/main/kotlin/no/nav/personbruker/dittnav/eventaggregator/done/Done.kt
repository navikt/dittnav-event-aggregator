package no.nav.personbruker.dittnav.eventaggregator.done

import java.time.LocalDateTime

data class Done(
        val systembruker: String,
        val namespace: String,
        val appnavn: String,
        val eventId: String,
        val eventTidspunkt: LocalDateTime,
        val forstBehandlet: LocalDateTime,
        val fodselsnummer: String,
        val grupperingsId: String,
        val sistBehandlet: LocalDateTime
)
