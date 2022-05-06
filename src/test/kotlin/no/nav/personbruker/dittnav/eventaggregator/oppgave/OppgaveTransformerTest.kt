package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.personbruker.dittnav.eventaggregator.common.toEpochMilli
import org.amshove.kluent.`should be empty`

import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should not be null`
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class OppgaveTransformerTest {

    private val eventId = 1
    private val dummyNokkel = createNokkel(eventId)

    @Test
    fun `should transform external to internal`() {
        val external = AvroOppgaveObjectMother.createOppgave(eventId)

        val internal = OppgaveTransformer.toInternal(dummyNokkel, external)

        internal.fodselsnummer `should be equal to` dummyNokkel.getFodselsnummer()
        internal.grupperingsId `should be equal to` dummyNokkel.getGrupperingsId()
        internal.eventId `should be equal to` dummyNokkel.getEventId()
        internal.link `should be equal to` external.getLink()
        internal.tekst `should be equal to` external.getTekst()
        internal.systembruker `should be equal to` dummyNokkel.getSystembruker()
        internal.sikkerhetsnivaa `should be equal to` external.getSikkerhetsnivaa()
        internal.namespace `should be equal to` dummyNokkel.getNamespace()
        internal.appnavn `should be equal to` dummyNokkel.getAppnavn()

        val transformedEventTidspunktAsLong = internal.eventTidspunkt.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        transformedEventTidspunktAsLong `should be equal to` external.getTidspunkt()

        internal.aktiv `should be equal to` true
        internal.eksternVarsling `should be equal to` true
        internal.prefererteKanaler `should be equal to` external.getPrefererteKanaler()
        internal.sistOppdatert.`should not be null`()
        internal.id.`should be null`()
    }


    @Test
    fun `should allow prefererteKanaler to be empty`() {
        val eventId = 1
        val nokkel = createNokkel(eventId)
        val eventUtenPrefererteKanaler = AvroOppgaveObjectMother.createOppgaveWithEksternVarslingAndPrefererteKanaler(true, emptyList())
        val internal = OppgaveTransformer.toInternal(nokkel, eventUtenPrefererteKanaler)
        internal.prefererteKanaler.`should be empty`()
    }

    @Test
    fun `should set forstBehandlet to behandlet if not null`() {
        val tidspunkt = LocalDateTime.now()
        val behandlet = tidspunkt.minusSeconds(10)

        val oppgave = AvroOppgaveObjectMother.createOppgaveWithTidspunktAndBehandlet(tidspunkt.toEpochMilli(), behandlet.toEpochMilli())

        val transformed = OppgaveTransformer.toInternal(dummyNokkel, oppgave)

        transformed.eventTidspunkt.truncatedTo(ChronoUnit.MILLIS) `should be equal to` tidspunkt.truncatedTo(ChronoUnit.MILLIS)
        transformed.forstBehandlet.truncatedTo(ChronoUnit.MILLIS) `should be equal to` behandlet.truncatedTo(ChronoUnit.MILLIS)
    }

    @Test
    fun `should set forstBehandlet to tidspunkt if behandlet is null`() {
        val tidspunkt = LocalDateTime.now()
        val behandlet = null

        val oppgave = AvroOppgaveObjectMother.createOppgaveWithTidspunktAndBehandlet(tidspunkt.toEpochMilli(), behandlet)

        val transformed = OppgaveTransformer.toInternal(dummyNokkel, oppgave)

        transformed.eventTidspunkt.truncatedTo(ChronoUnit.MILLIS)  `should be equal to` tidspunkt.truncatedTo(ChronoUnit.MILLIS)
        transformed.forstBehandlet.truncatedTo(ChronoUnit.MILLIS)  `should be equal to` tidspunkt.truncatedTo(ChronoUnit.MILLIS)
    }

    @Test
    fun `should attempt to fix and set forstBehandlet if behandlet is null and tidspunkt appears truncated`() {
        val tidspunkt = LocalDateTime.now()
        val behandlet = null

        val truncatedTidspunkt = tidspunkt.toEpochSecond(ZoneOffset.UTC)

        val oppgave = AvroOppgaveObjectMother.createOppgaveWithTidspunktAndBehandlet(truncatedTidspunkt, behandlet)

        val transformed = OppgaveTransformer.toInternal(dummyNokkel, oppgave)

        transformed.eventTidspunkt.toEpochMilli() `should be equal to` truncatedTidspunkt
        transformed.forstBehandlet.truncatedTo(ChronoUnit.SECONDS) `should be equal to` tidspunkt.truncatedTo(ChronoUnit.SECONDS)
    }
}
