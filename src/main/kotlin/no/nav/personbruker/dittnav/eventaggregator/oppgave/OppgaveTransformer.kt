package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.serializer.getNonNullField
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object OppgaveTransformer {

    fun toInternal(nokkel: Nokkel, external : no.nav.brukernotifikasjon.schemas.Oppgave) : Oppgave {
        val newRecordsAreActiveByDefault = true
        val internal = Oppgave(
                nokkel.getSystembruker(),
                nokkel.getEventId(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(external.getTidspunkt()), ZoneId.of("UTC")),
                getNonNullField(external.getFodselsnummer(), "Fødselsnummer"),
                external.getGrupperingsId(),
                external.getTekst(),
                external.getLink(),
                external.getSikkerhetsnivaa(),
                LocalDateTime.now(ZoneId.of("UTC")),
                newRecordsAreActiveByDefault
        )
        return internal
    }
}