package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.brukernotifikasjon.schemas.internal.DoneIntern
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object DoneTransformer {

    fun toInternal(nokkel: NokkelIntern, external: DoneIntern): Done {
        return Done(nokkel.getSystembruker(),
                nokkel.getNamespace(),
                nokkel.getAppnavn(),
                nokkel.getEventId(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(external.getTidspunkt()), ZoneId.of("UTC")),
                nokkel.getFodselsnummer(),
                nokkel.getGrupperingsId()
        )
    }
}
