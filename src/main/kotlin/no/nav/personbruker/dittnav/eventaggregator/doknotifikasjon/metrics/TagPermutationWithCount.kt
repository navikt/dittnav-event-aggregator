package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.metrics

data class TagPermutationWithCount(
    val eventType: String,
    val producer: String,
    val status: String,
    val count: Int
)
