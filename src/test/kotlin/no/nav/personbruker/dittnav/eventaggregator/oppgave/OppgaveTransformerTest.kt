package no.nav.personbruker.dittnav.eventaggregator.oppgave

import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should not be null`
import org.junit.jupiter.api.Test
import java.time.ZoneId

class OppgaveTransformerTest {

    @Test
    fun `should transform external to internal`() {
        val external = AvroOppgaveObjectMother.createOppgave(1)

        val internal = OppgaveTransformer.toInternal(external)

        internal.aktorId `should be equal to` external.getFodselsnummer()
        internal.dokumentId `should be equal to` external.getGrupperingsId()
        internal.eventId `should be equal to` external.getEventId()
        internal.link `should be equal to` external.getLink()
        internal.tekst `should be equal to` external.getTekst()
        internal.produsent `should be equal to` external.getProdusent()
        internal.sikkerhetsinvaa `should be equal to` external.getSikkerhetsnivaa()

        val transformedEventTidspunktAsLong = internal.eventTidspunkt.atZone(ZoneId.of("Europe/Oslo")).toInstant().toEpochMilli()
        transformedEventTidspunktAsLong `should be equal to` external.getTidspunkt()

        internal.aktiv `should be` true
        internal.sistOppdatert.`should not be null`()
        internal.id.`should be null`()
    }
}