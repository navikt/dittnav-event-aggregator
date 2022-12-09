package no.nav.personbruker.dittnav.eventaggregator.varsel

data class VarselIdentifier(
        val eventId: String,
        val systembruker: String,
        val type: VarselType,
        val fodselsnummer: String
)
