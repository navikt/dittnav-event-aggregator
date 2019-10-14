package no.nav.personbruker.dittnav.eventaggregator.common.database.entity

import no.nav.personbruker.dittnav.eventaggregator.config.EventType

data class Brukernotifikasjon(
    val id: String,
    val produsent: String,
    val type: EventType
)
