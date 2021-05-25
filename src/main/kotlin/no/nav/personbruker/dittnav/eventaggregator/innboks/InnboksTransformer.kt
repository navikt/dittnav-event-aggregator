package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.brukernotifikasjon.schemas.internal.InnboksIntern
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object InnboksTransformer {

    private const val newRecordsAreActiveByDefault = true

    fun toInternal(nokkel: NokkelIntern, external: InnboksIntern): Innboks {
        return Innboks(
                nokkel.getSystembruker(),
                nokkel.getEventId(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(external.getTidspunkt()), ZoneId.of("UTC")),
                nokkel.getFodselsnummer(),
                external.getGrupperingsId(),
                external.getTekst(),
                external.getLink(),
                external.getSikkerhetsnivaa(),
                LocalDateTime.now(ZoneId.of("UTC")),
                newRecordsAreActiveByDefault
        )
    }

}