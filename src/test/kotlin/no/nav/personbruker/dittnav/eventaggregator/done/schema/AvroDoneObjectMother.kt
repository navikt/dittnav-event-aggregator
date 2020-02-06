package no.nav.personbruker.dittnav.eventaggregator.done.schema

import no.nav.brukernotifikasjon.schemas.Done
import java.time.Instant

object AvroDoneObjectMother {

    fun createDone(eventId: String): Done {
        return Done(
                Instant.now().toEpochMilli(),
                "12345",
                "100${eventId}"
        )
    }
}
