package no.nav.personbruker.dittnav.eventaggregator.entity.objectmother

import no.nav.personbruker.dittnav.eventaggregator.database.entity.Done
import java.time.LocalDateTime
import java.time.ZoneId

object DoneObjectMother {

    fun createDone(eventId: String): Done {
        return Done(
                eventId,
                "DittNav",
                LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                "12345",
                "100${eventId}"
        )
    }
}
