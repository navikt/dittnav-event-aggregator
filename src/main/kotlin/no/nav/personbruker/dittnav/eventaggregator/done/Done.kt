package no.nav.personbruker.dittnav.eventaggregator.done

import java.time.LocalDateTime

data class Done(
        val eventId: String,
        val produsent: String,
        val eventTidspunkt: LocalDateTime,
        val fodselsnummer: String,
        val grupperingsId: String
)
