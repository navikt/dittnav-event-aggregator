package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.brukernotifikasjon.schemas.internal.OppgaveIntern
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object OppgaveTransformer {

    private const val newRecordsAreActiveByDefault = true

    fun toInternal(nokkel: NokkelIntern, external: OppgaveIntern): Oppgave {
        val synligFremTil: LocalDateTime? = if (external.getSynligFremTil() != null) {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(external.getSynligFremTil()), ZoneId.of("UTC"))
        } else {
            null
        }
        return Oppgave(
                nokkel.getSystembruker(),
                nokkel.getNamespace(),
                nokkel.getAppnavn(),
                nokkel.getEventId(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(external.getTidspunkt()), ZoneId.of("UTC")),
                nokkel.getFodselsnummer(),
                nokkel.getGrupperingsId(),
                external.getTekst(),
                external.getLink(),
                external.getSikkerhetsnivaa(),
                LocalDateTime.now(ZoneId.of("UTC")),
                newRecordsAreActiveByDefault,
                external.getEksternVarsling(),
                external.getPrefererteKanaler(),
                synligFremTil
        )
    }
}
