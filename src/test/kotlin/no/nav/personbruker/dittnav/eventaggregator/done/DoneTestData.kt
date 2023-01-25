package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowTruncatedToMillis
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselHeader

object DoneTestData {
    fun done(eventId: String, fodselsnummer: String= "123", namespace: String = "dummyNamespace", appnavn: String = "dummyAppnavn"): Done {
        return Done(
            systembruker = "dummySystembruker",
            namespace = namespace,
            appnavn = appnavn,
            eventId = eventId,
            eventTidspunkt = nowTruncatedToMillis(),
            forstBehandlet = nowTruncatedToMillis(),
            fodselsnummer = fodselsnummer,
            grupperingsId = "100${eventId}",
            sistBehandlet = nowTruncatedToMillis()
        )
    }

    fun matchingDoneEvent(varsel: VarselHeader): Done {
        return done(
            varsel.eventId,
            varsel.fodselsnummer,
            varsel.namespace,
            varsel.appnavn
        )
    }

}
