package no.nav.personbruker.dittnav.eventaggregator.transformer

import no.nav.personbruker.dittnav.eventaggregator.database.entity.Informasjon
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class InformasjonTransformer {

    fun toInternal(external: no.nav.personbruker.dittnav.event.schemas.Informasjon) : Informasjon {
        val newRecordsAreActiveByDefault = true
        val internal = Informasjon(external.getProdusent(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(external.getTidspunkt()), ZoneId.systemDefault()),
                external.getAktorId(),
                external.getEventId(),
                external.getDokumentId(),
                external.getTekst(),
                external.getLink(),
                external.getSikkerhetsniva(),
                LocalDateTime.now(),
                newRecordsAreActiveByDefault
        )
        return internal
    }

}
