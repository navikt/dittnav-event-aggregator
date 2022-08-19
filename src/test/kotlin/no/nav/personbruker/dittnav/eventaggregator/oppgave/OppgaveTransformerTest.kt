package no.nav.personbruker.dittnav.eventaggregator.oppgave

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowTruncatedToMillis
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.toEpochMilli
import no.nav.personbruker.dittnav.eventaggregator.nokkel.AvroNokkelInternObjectMother
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class OppgaveTransformerTest {

    private val eventId = 1
    private val dummyNokkel = AvroNokkelInternObjectMother.createNokkelWithEventId(eventId)

    @Test
    fun `should transform external to internal`() {
        val external = AvroOppgaveObjectMother.createOppgave(eventId)

        val internal = OppgaveTransformer.toInternal(dummyNokkel, external)

        internal.fodselsnummer shouldBe dummyNokkel.getFodselsnummer()
        internal.grupperingsId shouldBe dummyNokkel.getGrupperingsId()
        internal.eventId shouldBe dummyNokkel.getEventId()
        internal.link shouldBe external.getLink()
        internal.tekst shouldBe external.getTekst()
        internal.systembruker shouldBe dummyNokkel.getSystembruker()
        internal.sikkerhetsnivaa shouldBe external.getSikkerhetsnivaa()
        internal.namespace shouldBe dummyNokkel.getNamespace()
        internal.appnavn shouldBe dummyNokkel.getAppnavn()

        val transformedEventTidspunktAsLong = internal.eventTidspunkt.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        transformedEventTidspunktAsLong shouldBe external.getTidspunkt()

        internal.aktiv shouldBe true
        internal.eksternVarsling shouldBe true
        internal.prefererteKanaler shouldBe external.getPrefererteKanaler()
        internal.sistOppdatert.shouldNotBeNull()
        internal.id.shouldBeNull()
    }


    @Test
    fun `should allow prefererteKanaler to be empty`() {
        val eventId = 1
        val nokkel = AvroNokkelInternObjectMother.createNokkelWithEventId(eventId)
        val eventUtenPrefererteKanaler = AvroOppgaveObjectMother.createOppgaveWithEksternVarslingAndPrefererteKanaler(true, emptyList())
        val internal = OppgaveTransformer.toInternal(nokkel, eventUtenPrefererteKanaler)
        internal.prefererteKanaler.shouldBeEmpty()
    }

    @Test
    fun `should set forstBehandlet to behandlet if not null`() {
        val tidspunkt = nowTruncatedToMillis()
        val behandlet = tidspunkt.minusSeconds(10)

        val oppgave = AvroOppgaveObjectMother.createOppgaveWithTidspunktAndBehandlet(tidspunkt.toEpochMilli(), behandlet.toEpochMilli())

        val transformed = OppgaveTransformer.toInternal(dummyNokkel, oppgave)

        transformed.eventTidspunkt shouldBe tidspunkt
        transformed.forstBehandlet shouldBe behandlet
    }

    @Test
    fun `should set forstBehandlet to tidspunkt if behandlet is null`() {
        val tidspunkt = nowTruncatedToMillis()
        val behandlet = null

        val oppgave = AvroOppgaveObjectMother.createOppgaveWithTidspunktAndBehandlet(tidspunkt.toEpochMilli(), behandlet)

        val transformed = OppgaveTransformer.toInternal(dummyNokkel, oppgave)

        transformed.eventTidspunkt shouldBe tidspunkt
        transformed.forstBehandlet shouldBe tidspunkt
    }

    @Test
    fun `should attempt to fix and set forstBehandlet if behandlet is null and tidspunkt appears truncated`() {
        val tidspunkt = nowTruncatedToMillis()
        val behandlet = null

        val truncatedTidspunkt = tidspunkt.toEpochSecond(ZoneOffset.UTC)

        val oppgave = AvroOppgaveObjectMother.createOppgaveWithTidspunktAndBehandlet(truncatedTidspunkt, behandlet)

        val transformed = OppgaveTransformer.toInternal(dummyNokkel, oppgave)

        transformed.eventTidspunkt.toEpochMilli() shouldBe truncatedTidspunkt
        transformed.forstBehandlet.truncatedTo(ChronoUnit.SECONDS) shouldBe tidspunkt.truncatedTo(ChronoUnit.SECONDS)
    }
}
