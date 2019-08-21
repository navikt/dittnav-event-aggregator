package no.nav.personbruker.dittnav.eventaggregator.transformer

import no.nav.personbruker.dittnav.eventaggregator.database.entity.Informasjon
import java.util.*

class InformasjonTransformer {

    fun toInternal(external: no.nav.personbruker.dittnav.event.schemas.Informasjon) : Informasjon {
        val newRecordsAreActiveByDefault = true
        val internal = Informasjon(external.getProdusent(),
                Date(external.getTidspunkt()),
                external.getAktorId(),
                external.getEventId(),
                external.getDokumentId(),
                external.getTekst(),
                external.getLink(),
                external.getSikkerhetsniva(),
                Date(),
                newRecordsAreActiveByDefault
        )
        return internal
    }

}
