package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType

data class EksternStatusOppdatering(
    val status: EksternStatus,
    val eventId: String,
    val varselType: VarselType,
    val namespace: String,
    val appnavn: String,
    val kanal: String?,
    val renotifikasjon: Boolean?
)


