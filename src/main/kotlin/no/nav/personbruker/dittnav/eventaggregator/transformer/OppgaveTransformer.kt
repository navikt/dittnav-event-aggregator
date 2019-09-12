package no.nav.personbruker.dittnav.eventaggregator.transformer

import no.nav.personbruker.dittnav.eventaggregator.database.entity.Oppgave
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class OppgaveTransformer {

    fun toInternal(external : no.nav.brukernotifikasjon.schemas.Oppgave) : Oppgave {
        val internal = Oppgave(
                external.getProdusent(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(external.getTidspunkt()), ZoneId.of("Europe/Oslo")),
                external.getAktorId(),
                external.getEventId(),
                external.getDokumentId(),
                external.getTekst(),
                external.getLink(),
                external.getSikkerhetsniva()
                )
        return internal
    }
}