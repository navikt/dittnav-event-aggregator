package no.nav.personbruker.dittnav.eventaggregator.beskjed

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.FieldNullException
import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import org.amshove.kluent.*
import org.junit.jupiter.api.Test
import java.time.ZoneId

class BeskjedTransformerTest {

    @Test
    fun `should transform form external to internal`() {
        val eventId = 1
        val original = AvroBeskjedObjectMother.createBeskjed(eventId)
        val nokkel = createNokkel(eventId)

        val transformed = BeskjedTransformer.toInternal(nokkel, original)

        transformed.fodselsnummer `should be equal to` original.getFodselsnummer()
        transformed.grupperingsId `should be equal to` original.getGrupperingsId()
        transformed.eventId `should be equal to` nokkel.getEventId()
        transformed.link `should be equal to` original.getLink()
        transformed.tekst `should be equal to` original.getTekst()
        transformed.produsent `should be equal to` nokkel.getSystembruker()
        transformed.sikkerhetsnivaa `should be equal to` original.getSikkerhetsnivaa()

        val transformedEventTidspunktAsLong = transformed.eventTidspunkt.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        transformedEventTidspunktAsLong `should be equal to` original.getTidspunkt()

        transformed.aktiv `should be equal to` true
        transformed.sistOppdatert.`should not be null`()
        transformed.id.`should be null`()
    }

    @Test
    fun `should throw FieldNullException when fodselsnummer is empty`() {
        val fodselsnummerEmpty = ""
        val eventid = 1
        val event = AvroBeskjedObjectMother.createBeskjedWithFodselsnummer(eventid, fodselsnummerEmpty)
        val nokkel = createNokkel(eventid)

        invoking {
            runBlocking {
                BeskjedTransformer.toInternal(nokkel, event)
            }
        }`should throw` FieldNullException::class
    }
}
