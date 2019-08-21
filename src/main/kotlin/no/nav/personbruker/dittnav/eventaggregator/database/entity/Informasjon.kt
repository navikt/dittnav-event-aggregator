package no.nav.personbruker.dittnav.eventaggregator.database.entity

import java.time.LocalDateTime
import java.util.*


data class Informasjon(
        val id: Int?,
        val produsent: String,
        val eventTidspunkt: Date,
        val aktorId: String,
        val eventId: String,
        val dokumentId: String,
        val tekst: String,
        val link: String,
        val sikkerhetsnivaa: Int,
        val sistOppdatert: Date,
        val aktiv: Boolean
) {
    constructor(produsent: String,
                eventTidspunkt: Date,
                aktorId: String,
                eventId: String,
                dokumentId: String,
                tekst: String,
                link: String,
                sikkerhetsnivaa: Int,
                sistOppdatert: Date,
                aktiv: Boolean) : this(null, produsent, eventTidspunkt, aktorId, eventId, dokumentId, tekst, link,
            sikkerhetsnivaa, sistOppdatert, aktiv)
}
