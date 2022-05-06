package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.brukernotifikasjon.schemas.internal.DoneIntern
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.personbruker.dittnav.eventaggregator.common.epochMillisToLocalDateTime
import no.nav.personbruker.dittnav.eventaggregator.common.epochToLocalDateTimeFixIfTruncated
import java.time.LocalDateTime
import java.time.ZoneId

object DoneTransformer {

    fun toInternal(nokkel: NokkelIntern, external: DoneIntern): Done {
        return Done(
            systembruker = nokkel.getSystembruker(),
            namespace = nokkel.getNamespace(),
            appnavn = nokkel.getAppnavn(),
            eventId = nokkel.getEventId(),
            eventTidspunkt = epochMillisToLocalDateTime(external.getTidspunkt()),
            forstBehandlet = determineForstBehandlet(external),
            fodselsnummer = nokkel.getFodselsnummer(),
            grupperingsId = nokkel.getGrupperingsId(),
            sistBehandlet = LocalDateTime.now(ZoneId.of("UTC"))
        )
    }

    private fun determineForstBehandlet(done: DoneIntern): LocalDateTime {
        return if (done.getBehandlet() != null) {
            epochMillisToLocalDateTime(done.getBehandlet())
        } else {
            epochToLocalDateTimeFixIfTruncated(done.getTidspunkt())
        }
    }
}
