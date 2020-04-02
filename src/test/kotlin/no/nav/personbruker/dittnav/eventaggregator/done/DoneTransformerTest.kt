package no.nav.personbruker.dittnav.eventaggregator.done

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.FieldNullException
import no.nav.personbruker.dittnav.eventaggregator.done.schema.AvroDoneObjectMother
import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.invoking
import org.junit.jupiter.api.Test
import java.time.ZoneId

class DoneTransformerTest {

    @Test
    fun `should transform form external to internal`() {
        val eventId = "123"
        val original = AvroDoneObjectMother.createDone(eventId)
        val nokkel = createNokkel(123)
        val transformed = DoneTransformer.toInternal(nokkel, original)

        transformed.produsent `should be equal to` nokkel.getSystembruker()
        transformed.fodselsnummer `should be equal to` original.getFodselsnummer()
        transformed.grupperingsId `should be equal to` original.getGrupperingsId()
        transformed.eventId `should be equal to` nokkel.getEventId()

        val transformedEventTidspunktAsLong = transformed.eventTidspunkt.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        transformedEventTidspunktAsLong `should be equal to` original.getTidspunkt()
    }

    @Test
    fun `should throw FieldNullException when fodselsnummer is empty`() {
        val fodselsnummer = ""
        val eventId = "123"
        val event = AvroDoneObjectMother.createDoneWithFodselsnummer(eventId, fodselsnummer)
        val nokkel = createNokkel(123)

        invoking {
            runBlocking {
                DoneTransformer.toInternal(nokkel, event)
            }
        } `should throw` FieldNullException::class
    }
}
