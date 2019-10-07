package no.nav.personbruker.dittnav.eventaggregator.database.entity

import no.nav.personbruker.dittnav.eventaggregator.config.EventType

data class Brukernotifikasjon(
    val id: String,
    val produsent: String,
    val type: EventType
)
