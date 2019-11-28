package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.done.schema.AvroDoneObjectMother
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import java.time.ZoneId

class DoneTransformerTest {

    @Test
    fun `should transform form external to internal`() {
        val original = AvroDoneObjectMother.createDone("123")
        val transformed = DoneTransformer.toInternal(original)

        transformed.produsent `should be equal to` original.getProdusent()
        transformed.aktorId `should be equal to` original.getFodselsnummer()
        transformed.dokumentId `should be equal to` original.getGrupperingsId()
        transformed.eventId `should be equal to` original.getEventId()

        val transformedEventTidspunktAsLong = transformed.eventTidspunkt.atZone(ZoneId.of("Europe/Oslo")).toInstant().toEpochMilli()
        transformedEventTidspunktAsLong `should be equal to` original.getTidspunkt()
    }
}
