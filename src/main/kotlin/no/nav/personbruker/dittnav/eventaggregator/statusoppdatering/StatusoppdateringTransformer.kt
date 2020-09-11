package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import no.nav.brukernotifikasjon.schemas.Nokkel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object StatusoppdateringTransformer {

    fun toInternal(externalNokkel: Nokkel, externalValue: no.nav.brukernotifikasjon.schemas.Statusoppdatering): Statusoppdatering {
        return Statusoppdatering(
                externalNokkel.getSystembruker(),
                externalNokkel.getEventId(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(externalValue.getTidspunkt()), ZoneId.of("UTC")),
                externalValue.getFodselsnummer(),
                externalValue.getGrupperingsId(),
                externalValue.getLink(),
                externalValue.getSikkerhetsnivaa(),
                LocalDateTime.now(ZoneId.of("UTC")),
                externalValue.getStatusGlobal(),
                externalValue.getStatusIntern(),
                externalValue.getSakstema()
        )
    }
}
