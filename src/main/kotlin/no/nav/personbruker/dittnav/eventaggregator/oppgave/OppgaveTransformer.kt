package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.brukernotifikasjon.schemas.Nokkel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object OppgaveTransformer {

    private const val newRecordsAreActiveByDefault = true

    fun toInternal(nokkel: Nokkel, external: no.nav.brukernotifikasjon.schemas.Oppgave): Oppgave {
        return Oppgave(
                nokkel.getSystembruker(),
                nokkel.getEventId(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(external.getTidspunkt()), ZoneId.of("UTC")),
                external.getFodselsnummer(),
                external.getGrupperingsId(),
                external.getTekst(),
                external.getLink(),
                external.getSikkerhetsnivaa(),
                LocalDateTime.now(ZoneId.of("UTC")),
                newRecordsAreActiveByDefault,
                external.getEksternVarsling(),
                external.getPrefererteKanaler()
        )
    }
}
