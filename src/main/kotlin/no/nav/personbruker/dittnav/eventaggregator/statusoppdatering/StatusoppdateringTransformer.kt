package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.brukernotifikasjon.schemas.internal.StatusoppdateringIntern
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object StatusoppdateringTransformer {

    fun toInternal(externalNokkel: NokkelIntern, externalValue: StatusoppdateringIntern): Statusoppdatering {
        return Statusoppdatering(
                externalNokkel.getSystembruker(),
                externalNokkel.getEventId(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(externalValue.getTidspunkt()), ZoneId.of("UTC")),
                externalNokkel.getFodselsnummer(),
                externalNokkel.getGrupperingsId(),
                externalValue.getLink(),
                externalValue.getSikkerhetsnivaa(),
                LocalDateTime.now(ZoneId.of("UTC")),
                externalValue.getStatusGlobal(),
                externalValue.getStatusIntern(),
                externalValue.getSakstema()
        )
    }
}
