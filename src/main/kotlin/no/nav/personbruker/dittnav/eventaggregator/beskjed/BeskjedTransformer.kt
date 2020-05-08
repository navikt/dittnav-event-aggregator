package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.common.validation.timestampToUTCDateOrNull
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

object BeskjedTransformer {

    private const val newRecordsAreActiveByDefault = true

    fun toInternal(externalNokkel: Nokkel, externalValue: no.nav.brukernotifikasjon.schemas.Beskjed): Beskjed {
        return Beskjed(
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
                externalValue.synligFremTilAsUTCDateTime(),
                newRecordsAreActiveByDefault
        )
    }

    private fun createRandomStringUUID(): String {
        return UUID.randomUUID().toString()
    }

    private fun no.nav.brukernotifikasjon.schemas.Beskjed.synligFremTilAsUTCDateTime(): LocalDateTime? {
        return timestampToUTCDateOrNull(getSynligFremTil())
    }

}
