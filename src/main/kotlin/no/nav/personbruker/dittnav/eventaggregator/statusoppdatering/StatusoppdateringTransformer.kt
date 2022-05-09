package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.brukernotifikasjon.schemas.internal.StatusoppdateringIntern
import no.nav.personbruker.dittnav.eventaggregator.common.epochMillisToLocalDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.epochToLocalDateTimeFixIfTruncated
import java.time.LocalDateTime
import java.time.ZoneId

object StatusoppdateringTransformer {

    fun toInternal(externalNokkel: NokkelIntern, externalValue: StatusoppdateringIntern): Statusoppdatering {
        return Statusoppdatering(
                systembruker = externalNokkel.getSystembruker(),
                namespace = externalNokkel.getNamespace(),
                appnavn = externalNokkel.getAppnavn(),
                eventId = externalNokkel.getEventId(),
                eventTidspunkt = epochMillisToLocalDateTime(externalValue.getTidspunkt()),
                forstBehandlet = determineForstBehandlet(externalValue),
                fodselsnummer = externalNokkel.getFodselsnummer(),
                grupperingsId = externalNokkel.getGrupperingsId(),
                link = externalValue.getLink(),
                sikkerhetsnivaa = externalValue.getSikkerhetsnivaa(),
                sistOppdatert = LocalDateTime.now(ZoneId.of("UTC")),
                statusGlobal = externalValue.getStatusGlobal(),
                statusIntern = externalValue.getStatusIntern(),
                sakstema = externalValue.getSakstema()
        )
    }

    private fun determineForstBehandlet(statusoppdatering: StatusoppdateringIntern): LocalDateTime {
        return if (statusoppdatering.getBehandlet() != null) {
            epochMillisToLocalDateTime(statusoppdatering.getBehandlet())
        } else {
            epochToLocalDateTimeFixIfTruncated(statusoppdatering.getTidspunkt())
        }
    }
}
