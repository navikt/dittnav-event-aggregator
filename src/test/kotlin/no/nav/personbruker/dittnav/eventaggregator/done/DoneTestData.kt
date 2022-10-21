package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowTruncatedToMillis
import no.nav.personbruker.dittnav.eventaggregator.common.Brukernotifikasjon

object DoneTestData {
    private const val testFnr = "123"
    fun done(eventId: String, systembruker: String = "dummySystembruker", fodselsnummer: String= testFnr): Done {
        return Done(
            systembruker = systembruker,
            namespace = "dummyNamespace",
            appnavn = "dummyAppnavn",
            eventId = eventId,
            eventTidspunkt = nowTruncatedToMillis(),
            forstBehandlet = nowTruncatedToMillis(),
            fodselsnummer = fodselsnummer,
            grupperingsId = "100${eventId}",
            sistBehandlet = nowTruncatedToMillis()
        )
    }

    fun matchingDoneEvent(brukernotifikasjonToMatch: Brukernotifikasjon): Done {
        return done(
                brukernotifikasjonToMatch.eventId,
                brukernotifikasjonToMatch.systembruker,
                brukernotifikasjonToMatch.fodselsnummer
        )
    }

}
