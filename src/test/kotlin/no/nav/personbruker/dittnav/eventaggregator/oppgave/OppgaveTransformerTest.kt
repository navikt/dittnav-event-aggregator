package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should not be null`
import org.junit.jupiter.api.Test
import java.time.ZoneId

class OppgaveTransformerTest {

    @Test
    fun `should transform external to internal`() {
        val eventId = 1
        val external = AvroOppgaveObjectMother.createOppgave(eventId)
        val nokkel = createNokkel(eventId)

        val internal = OppgaveTransformer.toInternal(nokkel, external)

        internal.fodselsnummer `should be equal to` external.getFodselsnummer()
        internal.grupperingsId `should be equal to` external.getGrupperingsId()
        internal.eventId `should be equal to` nokkel.getEventId()
        internal.link `should be equal to` external.getLink()
        internal.tekst `should be equal to` external.getTekst()
        internal.systembruker `should be equal to` nokkel.getSystembruker()
        internal.sikkerhetsnivaa `should be equal to` external.getSikkerhetsnivaa()

        val transformedEventTidspunktAsLong = internal.eventTidspunkt.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        transformedEventTidspunktAsLong `should be equal to` external.getTidspunkt()

        internal.aktiv `should be equal to` true
        internal.eksternVarsling `should be equal to` true
        internal.sistOppdatert.`should not be null`()
        internal.id.`should be null`()
    }

}
