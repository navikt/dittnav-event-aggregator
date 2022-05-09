package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.brukernotifikasjon.schemas.internal.BeskjedIntern
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.personbruker.dittnav.eventaggregator.common.epochMillisToLocalDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.timestampToUTCDateOrNull
import no.nav.personbruker.dittnav.eventaggregator.common.epochToLocalDateTimeFixIfTruncated
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object BeskjedTransformer {

    private const val newRecordsAreActiveByDefault = true

    fun toInternal(externalNokkel: NokkelIntern, externalValue: BeskjedIntern): Beskjed {
        return Beskjed(
                systembruker = externalNokkel.getSystembruker(),
                namespace = externalNokkel.getNamespace(),
                appnavn = externalNokkel.getAppnavn(),
                eventId = externalNokkel.getEventId(),
                eventTidspunkt = epochMillisToLocalDateTime(externalValue.getTidspunkt()),
                forstBehandlet = determineForstBehandlet(externalValue),
                fodselsnummer = externalNokkel.getFodselsnummer(),
                grupperingsId = externalNokkel.getGrupperingsId(),
                tekst = externalValue.getTekst(),
                link = externalValue.getLink(),
                sikkerhetsnivaa = externalValue.getSikkerhetsnivaa(),
                sistOppdatert = LocalDateTime.now(ZoneId.of("UTC")),
                synligFremTil = externalValue.synligFremTilAsUTCDateTime(),
                aktiv = newRecordsAreActiveByDefault,
                eksternVarsling = externalValue.getEksternVarsling(),
                prefererteKanaler = externalValue.getPrefererteKanaler()
        )
    }

    private fun BeskjedIntern.synligFremTilAsUTCDateTime(): LocalDateTime? {
        return timestampToUTCDateOrNull(getSynligFremTil())
    }

    private fun determineForstBehandlet(beskjed: BeskjedIntern): LocalDateTime {
        return if (beskjed.getBehandlet() != null) {
            epochMillisToLocalDateTime(beskjed.getBehandlet())
        } else {
            epochToLocalDateTimeFixIfTruncated(beskjed.getTidspunkt())
        }
    }
}
