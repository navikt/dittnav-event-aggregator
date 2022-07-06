package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.brukernotifikasjon.schemas.internal.InnboksIntern
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.personbruker.dittnav.eventaggregator.common.epochMillisToLocalDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.epochToLocalDateTimeFixIfTruncated
import java.time.LocalDateTime
import java.time.ZoneId

object InnboksTransformer {

    private const val newRecordsAreActiveByDefault = true

    fun toInternal(nokkel: NokkelIntern, innboks: InnboksIntern): Innboks {
        return Innboks(
                systembruker = nokkel.getSystembruker(),
                namespace = nokkel.getNamespace(),
                appnavn = nokkel.getAppnavn(),
                eventId = nokkel.getEventId(),
                eventTidspunkt = epochMillisToLocalDateTime(innboks.getTidspunkt()),
                forstBehandlet = determineForstBehandlet(innboks),
                fodselsnummer = nokkel.getFodselsnummer(),
                grupperingsId = nokkel.getGrupperingsId(),
                tekst = innboks.getTekst(),
                link = innboks.getLink(),
                sikkerhetsnivaa = innboks.getSikkerhetsnivaa(),
                sistOppdatert = LocalDateTime.now(ZoneId.of("UTC")),
                aktiv = newRecordsAreActiveByDefault,
                eksternVarsling = innboks.getEksternVarsling(),
                prefererteKanaler = innboks.getPrefererteKanaler()
        )
    }

    private fun determineForstBehandlet(innboks: InnboksIntern): LocalDateTime {
        return if (innboks.getBehandlet() != null) {
            epochMillisToLocalDateTime(innboks.getBehandlet())
        } else {
            epochToLocalDateTimeFixIfTruncated(innboks.getTidspunkt())
        }
    }
}
