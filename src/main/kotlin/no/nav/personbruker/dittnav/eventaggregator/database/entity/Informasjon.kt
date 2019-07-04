package no.nav.personbruker.dittnav.eventaggregator.database.entity

import org.jetbrains.exposed.dao.EntityID
import org.joda.time.DateTime

data class Informasjon(
        val id: EntityID<Int>?,
        val produsent: String,
        val eventTidspunkt: DateTime,
        val aktorid: String,
        val eventId: String,
        val dokumentId: String,
        val tekst: String,
        val link: String,
        val sikkerhetsnivaa: Int,
        val sistOppdatert: DateTime,
        val aktiv: Boolean
) {
    constructor(produsent: String,
                eventTidspunkt: DateTime,
                aktorid: String,
                eventId: String,
                dokumentId: String,
                tekst: String,
                link: String,
                sikkerhetsnivaa: Int,
                sistOppdatert: DateTime,
                aktiv: Boolean) : this(null, produsent, eventTidspunkt, aktorid, eventId, dokumentId, tekst, link,
            sikkerhetsnivaa, sistOppdatert, aktiv)
}
