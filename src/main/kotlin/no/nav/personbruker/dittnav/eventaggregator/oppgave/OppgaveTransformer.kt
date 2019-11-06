package no.nav.personbruker.dittnav.eventaggregator.oppgave

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object OppgaveTransformer {

    fun toInternal(external : no.nav.brukernotifikasjon.schemas.Oppgave) : Oppgave {
        val newRecordsAreActiveByDefault = true
        val internal = Oppgave(
                external.getProdusent(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(external.getTidspunkt()), ZoneId.of("Europe/Oslo")),
                external.getAktorId(),
                external.getEventId(),
                external.getDokumentId(),
                external.getTekst(),
                external.getLink(),
                external.getSikkerhetsnivaa(),
                LocalDateTime.now(),
                newRecordsAreActiveByDefault
        )
        return internal
    }
}