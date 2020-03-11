package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.brukernotifikasjon.schemas.Nokkel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

object BeskjedTransformer {

    fun toInternal(externalNokkel: Nokkel, externalValue: no.nav.brukernotifikasjon.schemas.Beskjed): Beskjed {
        val newRecordsAreActiveByDefault = true
        val internal = Beskjed(
                createRandomStringUUID(),
                externalNokkel.getSystembruker(),
                externalNokkel.getEventId(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(externalValue.getTidspunkt()), ZoneId.of("UTC")),
                externalValue.getFodselsnummer(),
                externalValue.getGrupperingsId(),
                externalValue.getTekst(),
                externalValue.getLink(),
                externalValue.getSikkerhetsnivaa(),
                LocalDateTime.now(ZoneId.of("UTC")),
                externalValue.getAsUTDateTime(),
                newRecordsAreActiveByDefault
        )
        return internal
    }

    private fun no.nav.brukernotifikasjon.schemas.Beskjed.getAsUTDateTime(): LocalDateTime? {
        return getSynligFremTil()?.let { datetime -> LocalDateTime.ofInstant(Instant.ofEpochMilli(datetime), ZoneId.of("UTC")) }
    }

    private fun createRandomStringUUID(): String {
        return UUID.randomUUID().toString()

    }
}
