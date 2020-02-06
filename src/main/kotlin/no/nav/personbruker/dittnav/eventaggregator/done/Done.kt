package no.nav.personbruker.dittnav.eventaggregator.done

import java.time.LocalDateTime

data class Done(
        val produsent: String,
        val eventId: String,
        val eventTidspunkt: LocalDateTime,
        val fodselsnummer: String,
        val grupperingsId: String
)
