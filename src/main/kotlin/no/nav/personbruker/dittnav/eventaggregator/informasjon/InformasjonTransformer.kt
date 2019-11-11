package no.nav.personbruker.dittnav.eventaggregator.informasjon

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object InformasjonTransformer {

    fun toInternal(external: no.nav.brukernotifikasjon.schemas.Informasjon): Informasjon {
        val newRecordsAreActiveByDefault = true
        val internal = Informasjon(external.getProdusent(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(external.getTidspunkt()), ZoneId.of("Europe/Oslo")),
                external.getAktorId(),
                external.getEventId(),
                external.getDokumentId(),
                external.getTekst(),
                external.getLink(),
                external.getSikkerhetsnivaa(),
                LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                newRecordsAreActiveByDefault
        )
        return internal
    }

}
