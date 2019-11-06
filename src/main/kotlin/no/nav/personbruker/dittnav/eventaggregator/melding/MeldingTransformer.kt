package no.nav.personbruker.dittnav.eventaggregator.melding

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object MeldingTransformer {

    fun toInternal(external: no.nav.brukernotifikasjon.schemas.Melding): Melding {
        val newRecordsAreActiveByDefault = true
        return Melding(
                external.getProdusent(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(external.getTidspunkt()), ZoneId.of("Europe/Oslo")),
                external.getAktorId(),
                external.getEventId(),
                external.getDokumentId(),
                external.getTekst(),
                external.getLink(),
                external.getSikkerhetsnivaa(),
                newRecordsAreActiveByDefault
        )
    }

}