package no.nav.personbruker.dittnav.eventaggregator.done

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object DoneTransformer {

    fun toInternal(external: no.nav.brukernotifikasjon.schemas.Done) : Done {
        val internal = Done(external.getEventId(),
                external.getProdusent(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(external.getTidspunkt()), ZoneId.of("Europe/Oslo")),
                external.getAktorId(),
                external.getDokumentId()
        )
        return internal
    }
}
