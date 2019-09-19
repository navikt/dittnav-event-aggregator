package no.nav.personbruker.dittnav.eventaggregator.transformer

import no.nav.personbruker.dittnav.eventaggregator.database.entity.Done
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class DoneTransformer {

    fun toInternal(external: no.nav.brukernotifikasjon.schemas.Done) : Done {
        val internal = Done(external.getProdusent(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(external.getTidspunkt()), ZoneId.of("Europe/Oslo")),
                external.getAktorId(),
                external.getEventId(),
                external.getDokumentId()
        )
        return internal
    }
}