package no.nav.personbruker.dittnav.eventaggregator.transformer

import no.nav.personbruker.dittnav.eventaggregator.schema.objectmother.InformasjonObjectMother
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should not be null`
import org.junit.jupiter.api.Test
import java.time.ZoneId

internal class InformasjonTransformerTest {

    @Test
    fun `should transform form external to internal`() {
        val original = InformasjonObjectMother.createInformasjon(1)

        val transformer = InformasjonTransformer()

        val transformed = transformer.toInternal(original)

        transformed.aktorId `should be equal to` original.getAktorId()
        transformed.dokumentId `should be equal to` original.getDokumentId()
        transformed.eventId `should be equal to` original.getEventId()
        transformed.link `should be equal to` original.getLink()
        transformed.tekst `should be equal to` original.getTekst()
        transformed.produsent `should be equal to` original.getProdusent()
        transformed.sikkerhetsnivaa `should be equal to` original.getSikkerhetsniva()

        val transformedEventTidspunktAsLong = transformed.eventTidspunkt.atZone(ZoneId.of("Europe/Oslo")).toInstant().toEpochMilli()
        transformedEventTidspunktAsLong `should be equal to` original.getTidspunkt()

        transformed.aktiv `should be equal to` true
        transformed.sistOppdatert.`should not be null`()
        transformed.id.`should be null`()
    }
}