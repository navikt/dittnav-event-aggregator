package no.nav.personbruker.dittnav.eventaggregator.varsel

data class VarselHeader(
        val eventId: String,
        val type: VarselType,
        val fodselsnummer: String,
        val namespace: String,
        val appnavn: String
)
