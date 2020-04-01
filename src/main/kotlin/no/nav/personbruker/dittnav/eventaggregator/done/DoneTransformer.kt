package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.serializer.getNonNullField
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object DoneTransformer {

    fun toInternal(nokkel: Nokkel, external: no.nav.brukernotifikasjon.schemas.Done) : Done {
        val internal = Done(nokkel.getSystembruker(),
                nokkel.getEventId(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(external.getTidspunkt()), ZoneId.of("UTC")),
                getNonNullField(external.getFodselsnummer(), "Fødselsnummer"),
                external.getGrupperingsId()
        )
        return internal
    }
}
