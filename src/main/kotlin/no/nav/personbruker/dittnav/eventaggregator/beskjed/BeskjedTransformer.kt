package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.brukernotifikasjon.schemas.internal.BeskjedIntern
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.personbruker.dittnav.eventaggregator.common.epochMillisToLocalDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.timestampToUTCDateOrNull
import no.nav.personbruker.dittnav.eventaggregator.common.epochToLocalDateTimeFixIfTruncated
import java.time.LocalDateTime
import java.time.ZoneId

object BeskjedTransformer {

    private const val newRecordsAreActiveByDefault = true

    fun toInternal(nokkel: NokkelIntern, beskjed: BeskjedIntern): Beskjed {
        return Beskjed(
                systembruker = nokkel.getSystembruker(),
                namespace = nokkel.getNamespace(),
                appnavn = nokkel.getAppnavn(),
                eventId = nokkel.getEventId(),
                eventTidspunkt = epochMillisToLocalDateTime(beskjed.getTidspunkt()),
                forstBehandlet = determineForstBehandlet(nokkel, beskjed),
                fodselsnummer = nokkel.getFodselsnummer(),
                grupperingsId = nokkel.getGrupperingsId(),
                tekst = beskjed.getTekst(),
                link = beskjed.getLink(),
                sikkerhetsnivaa = beskjed.getSikkerhetsnivaa(),
                sistOppdatert = LocalDateTime.now(ZoneId.of("UTC")),
                synligFremTil = beskjed.synligFremTilAsUTCDateTime(),
                aktiv = determineAktiv(nokkel),
                eksternVarsling = beskjed.getEksternVarsling(),
                prefererteKanaler = beskjed.getPrefererteKanaler()
        )
    }

    private fun BeskjedIntern.synligFremTilAsUTCDateTime(): LocalDateTime? {
        return timestampToUTCDateOrNull(getSynligFremTil())
    }

    private fun determineForstBehandlet(nokkel: NokkelIntern, beskjed: BeskjedIntern): LocalDateTime {
        return if(nokkel.getAppnavn() == "varselinnboks") {
            epochMillisToLocalDateTime(beskjed.getTidspunkt())
        } else if (beskjed.getBehandlet() != null) {
            epochMillisToLocalDateTime(beskjed.getBehandlet())
        } else {
            epochToLocalDateTimeFixIfTruncated(beskjed.getTidspunkt())
        }
    }

    private fun determineAktiv(nokkel: NokkelIntern): Boolean {
        return if(nokkel.getAppnavn() == "varselinnboks" && nokkel.getGrupperingsId() == "lest") {
            false
        } else {
            newRecordsAreActiveByDefault
        }
    }
}
