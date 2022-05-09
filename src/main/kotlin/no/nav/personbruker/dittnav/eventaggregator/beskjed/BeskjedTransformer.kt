package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.brukernotifikasjon.schemas.internal.BeskjedIntern
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.personbruker.dittnav.eventaggregator.common.validation.timestampToUTCDateOrNull
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

object BeskjedTransformer {

    private const val newRecordsAreActiveByDefault = true

    fun toInternal(externalNokkel: NokkelIntern, externalValue: BeskjedIntern): Beskjed {
        return Beskjed(
                externalNokkel.getSystembruker(),
                externalNokkel.getNamespace(),
                externalNokkel.getAppnavn(),
                externalNokkel.getEventId(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(externalValue.getTidspunkt()), ZoneId.of("UTC")),
                externalNokkel.getFodselsnummer(),
                externalNokkel.getGrupperingsId(),
                externalValue.getTekst(),
                externalValue.getLink(),
                externalValue.getSikkerhetsnivaa(),
                LocalDateTime.now(ZoneId.of("UTC")),
                externalValue.synligFremTilAsUTCDateTime(),
                newRecordsAreActiveByDefault,
                externalValue.getEksternVarsling(),
                externalValue.getPrefererteKanaler()
        )
    }

    private fun BeskjedIntern.synligFremTilAsUTCDateTime(): LocalDateTime? {
        return timestampToUTCDateOrNull(getSynligFremTil())
    }

}
