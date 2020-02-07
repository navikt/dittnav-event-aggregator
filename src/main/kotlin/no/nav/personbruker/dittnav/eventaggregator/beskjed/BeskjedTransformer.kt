package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.brukernotifikasjon.schemas.Nokkel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object BeskjedTransformer {

    fun toInternal(nokkel: Nokkel, external: no.nav.brukernotifikasjon.schemas.Beskjed): Beskjed {
        val newRecordsAreActiveByDefault = true
        val internal = Beskjed(nokkel.getSystembruker(),
                nokkel.getEventId(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(external.getTidspunkt()), ZoneId.of("Europe/Oslo")),
                external.getFodselsnummer(),
                external.getGrupperingsId(),
                external.getTekst(),
                external.getLink(),
                external.getSikkerhetsnivaa(),
                LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                external.getAsTimeZoneOslo(),
                newRecordsAreActiveByDefault
        )
        return internal
    }

    private fun no.nav.brukernotifikasjon.schemas.Beskjed.getAsTimeZoneOslo(): LocalDateTime? {
        return getSynligFremTil()?.let { datetime -> LocalDateTime.ofInstant(Instant.ofEpochMilli(datetime), ZoneId.of("Europe/Oslo")) }
    }
}
