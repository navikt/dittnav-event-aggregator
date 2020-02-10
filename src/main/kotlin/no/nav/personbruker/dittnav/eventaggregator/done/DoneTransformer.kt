package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.brukernotifikasjon.schemas.Nokkel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object DoneTransformer {

    fun toInternal(nokkel: Nokkel, external: no.nav.brukernotifikasjon.schemas.Done) : Done {
        val internal = Done(nokkel.getSystembruker(),
                nokkel.getEventId(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(external.getTidspunkt()), ZoneId.of("Europe/Oslo")),
                external.getFodselsnummer(),
                external.getGrupperingsId()
        )
        return internal
    }
}
