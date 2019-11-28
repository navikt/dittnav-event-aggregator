package no.nav.personbruker.dittnav.eventaggregator.beskjed

import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should not be null`
import org.junit.jupiter.api.Test
import java.time.ZoneId

class BeskjedTransformerTest {

    @Test
    fun `should transform form external to internal`() {
        val original = AvroBeskjedObjectMother.createBeskjed(1)

        val transformed = BeskjedTransformer.toInternal(original)

        transformed.aktorId `should be equal to` original.getFodselsnummer()
        transformed.dokumentId `should be equal to` original.getGrupperingsId()
        transformed.eventId `should be equal to` original.getEventId()
        transformed.link `should be equal to` original.getLink()
        transformed.tekst `should be equal to` original.getTekst()
        transformed.produsent `should be equal to` original.getProdusent()
        transformed.sikkerhetsnivaa `should be equal to` original.getSikkerhetsnivaa()

        val transformedEventTidspunktAsLong = transformed.eventTidspunkt.atZone(ZoneId.of("Europe/Oslo")).toInstant().toEpochMilli()
        transformedEventTidspunktAsLong `should be equal to` original.getTidspunkt()

        transformed.aktiv `should be equal to` true
        transformed.sistOppdatert.`should not be null`()
        transformed.id.`should be null`()
    }
}
