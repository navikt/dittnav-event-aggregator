package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.brukernotifikasjon.schemas.Nokkel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object BeskjedTransformer {

    fun toInternal(externalNokkel: Nokkel, externalValue: no.nav.brukernotifikasjon.schemas.Beskjed): Beskjed {
        val newRecordsAreActiveByDefault = true
        val internal = Beskjed(externalNokkel.getSystembruker(),
                externalNokkel.getEventId(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(externalValue.getTidspunkt()), ZoneId.of("Europe/Oslo")),
                externalValue.getFodselsnummer(),
                externalValue.getGrupperingsId(),
                externalValue.getTekst(),
                externalValue.getLink(),
                externalValue.getSikkerhetsnivaa(),
                LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                externalValue.getAsTimeZoneOslo(),
                newRecordsAreActiveByDefault
        )
        return internal
    }

    private fun no.nav.brukernotifikasjon.schemas.Beskjed.getAsTimeZoneOslo(): LocalDateTime? {
        return getSynligFremTil()?.let { datetime -> LocalDateTime.ofInstant(Instant.ofEpochMilli(datetime), ZoneId.of("Europe/Oslo")) }
    }
}
