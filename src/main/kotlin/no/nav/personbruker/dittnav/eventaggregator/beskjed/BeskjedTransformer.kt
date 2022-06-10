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

    fun toInternal(nokkelIntern: NokkelIntern, beskjedIntern: BeskjedIntern): Beskjed {
        return Beskjed(
                systembruker = nokkelIntern.getSystembruker(),
                namespace = nokkelIntern.getNamespace(),
                appnavn = nokkelIntern.getAppnavn(),
                eventId = nokkelIntern.getEventId(),
                eventTidspunkt = epochMillisToLocalDateTime(beskjedIntern.getTidspunkt()),
                forstBehandlet = determineForstBehandlet(beskjedIntern),
                fodselsnummer = nokkelIntern.getFodselsnummer(),
                grupperingsId = nokkelIntern.getGrupperingsId(),
                tekst = beskjedIntern.getTekst(),
                link = beskjedIntern.getLink(),
                sikkerhetsnivaa = beskjedIntern.getSikkerhetsnivaa(),
                sistOppdatert = LocalDateTime.now(ZoneId.of("UTC")),
                synligFremTil = beskjedIntern.synligFremTilAsUTCDateTime(),
                aktiv = determineAktiv(nokkelIntern),
                eksternVarsling = beskjedIntern.getEksternVarsling(),
                prefererteKanaler = beskjedIntern.getPrefererteKanaler()
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

    private fun determineAktiv(nokkelIntern: NokkelIntern): Boolean {
        return if(nokkelIntern.getAppnavn() == "varselinnboks" && nokkelIntern.getGrupperingsId() == "lest") {
            false
        } else {
            newRecordsAreActiveByDefault
        }
    }
}
