package no.nav.personbruker.dittnav.eventaggregator.transformer

import no.nav.personbruker.dittnav.eventaggregator.database.entity.Informasjon
import org.joda.time.DateTime

class InformasjonTransformer {

    fun toInternal(external: no.nav.brukernotifikasjon.schemas.Informasjon) : Informasjon {
        val newRecordsAreActiveByDefault = true
        val internal = Informasjon(external.getProdusent(),
                DateTime(external.getTidspunkt()),
                external.getAktorId(),
                external.getEventId(),
                external.getDokumentId(),
                external.getTekst(),
                external.getLink(),
                external.getSikkerhetsniva(),
                DateTime.now(),
                newRecordsAreActiveByDefault
        )
        return internal
    }

}
