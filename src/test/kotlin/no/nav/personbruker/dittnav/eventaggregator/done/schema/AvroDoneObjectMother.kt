package no.nav.personbruker.dittnav.eventaggregator.done.schema

import no.nav.brukernotifikasjon.schemas.Done
import java.time.Instant

object AvroDoneObjectMother {

    fun createDone(eventId: String): Done {
        return Done(
                "DittNav",
                Instant.now().toEpochMilli(),
                "12345",
                eventId,
                "100${eventId}"
        )
    }
}
