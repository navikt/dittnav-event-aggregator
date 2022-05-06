package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.brukernotifikasjon.schemas.internal.OppgaveIntern
import no.nav.personbruker.dittnav.eventaggregator.common.epochMillisToLocalDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.epochToLocalDateTimeFixIfTruncated
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
                systembruker = nokkel.getSystembruker(),
                namespace = nokkel.getNamespace(),
                appnavn = nokkel.getAppnavn(),
                eventId = nokkel.getEventId(),
                eventTidspunkt = epochMillisToLocalDateTime(external.getTidspunkt()),
                forstBehandlet = determineForstBehandlet(external),
                fodselsnummer = nokkel.getFodselsnummer(),
                grupperingsId = nokkel.getGrupperingsId(),
                tekst = external.getTekst(),
                link = external.getLink(),
                sikkerhetsnivaa = external.getSikkerhetsnivaa(),
                sistOppdatert = LocalDateTime.now(ZoneId.of("UTC")),
                aktiv = newRecordsAreActiveByDefault,
                eksternVarsling = external.getEksternVarsling(),
                prefererteKanaler = external.getPrefererteKanaler(),
                synligFremTil = synligFremTil
        )
    }

    private fun determineForstBehandlet(oppgave: OppgaveIntern): LocalDateTime {
        return if (oppgave.getBehandlet() != null) {
            epochMillisToLocalDateTime(oppgave.getBehandlet())
        } else {
            epochToLocalDateTimeFixIfTruncated(oppgave.getTidspunkt())
        }
    }
}
