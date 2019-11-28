package no.nav.personbruker.dittnav.eventaggregator.innboks

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object InnboksTransformer {

    fun toInternal(external: no.nav.brukernotifikasjon.schemas.Innboks): Innboks {
        val newRecordsAreActiveByDefault = true
        return Innboks(
                external.getProdusent(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(external.getTidspunkt()), ZoneId.of("Europe/Oslo")),
                external.getFodselsnummer(),
                external.getEventId(),
                external.getGrupperingsId(),
                external.getTekst(),
                external.getLink(),
                external.getSikkerhetsnivaa(),
                LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                newRecordsAreActiveByDefault
        )
    }

}