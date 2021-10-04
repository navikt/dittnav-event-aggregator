package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.done.schema.AvroDoneObjectMother
import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import java.time.ZoneId

class DoneTransformerTest {

    @Test
    fun `should transform form external to internal`() {
        val eventId = 123
        val original = AvroDoneObjectMother.createDone()
        val nokkel = createNokkel(eventId)
        val transformed = DoneTransformer.toInternal(nokkel, original)

        transformed.systembruker `should be equal to` nokkel.getSystembruker()
        transformed.fodselsnummer `should be equal to` nokkel.getFodselsnummer()
        transformed.grupperingsId `should be equal to` nokkel.getGrupperingsId()
        transformed.eventId `should be equal to` nokkel.getEventId()

        val transformedEventTidspunktAsLong = transformed.eventTidspunkt.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        transformedEventTidspunktAsLong `should be equal to` original.getTidspunkt()
    }

}
