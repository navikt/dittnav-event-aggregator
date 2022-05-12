package no.nav.personbruker.dittnav.eventaggregator.beskjed

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import no.nav.personbruker.dittnav.eventaggregator.common.toEpochMilli
import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit.MILLIS
import java.time.temporal.ChronoUnit.SECONDS

class BeskjedTransformerTest {

    private val dummyNokkel = createNokkel(1)

    @Test
    fun `should transform form external to internal`() {
        val eventId = 1
        val original = AvroBeskjedObjectMother.createBeskjed(eventId)
        val nokkel = createNokkel(eventId)

        val transformed = BeskjedTransformer.toInternal(nokkel, original)

        transformed.fodselsnummer shouldBe nokkel.getFodselsnummer()
        transformed.grupperingsId shouldBe nokkel.getGrupperingsId()
        transformed.eventId shouldBe nokkel.getEventId()
        transformed.link shouldBe original.getLink()
        transformed.tekst shouldBe original.getTekst()
        transformed.systembruker shouldBe nokkel.getSystembruker()
        transformed.sikkerhetsnivaa shouldBe original.getSikkerhetsnivaa()
        transformed.appnavn shouldBe nokkel.getAppnavn()
        transformed.namespace shouldBe nokkel.getNamespace()

        val transformedEventTidspunktAsLong = transformed.eventTidspunkt.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        transformedEventTidspunktAsLong shouldBe original.getTidspunkt()

        transformed.aktiv shouldBe true
        transformed.eksternVarsling shouldBe true
        transformed.prefererteKanaler shouldBe original.getPrefererteKanaler()
        transformed.sistOppdatert.shouldNotBeNull()
        transformed.id.shouldBeNull()
    }

    @Test
    fun `should allow synligFremTil to be null`() {
        val beskjedUtenSynligTilSatt = AvroBeskjedObjectMother.createBeskjedWithoutSynligFremTilSatt()

        val transformed = BeskjedTransformer.toInternal(dummyNokkel, beskjedUtenSynligTilSatt)

        transformed.synligFremTil.shouldBeNull()
    }

    @Test
    fun `should allow prefererteKanaler to be empty`() {
        val beskjedUtenPrefererteKanaler = AvroBeskjedObjectMother.createBeskjedWithEksternVarslingAndPrefererteKanaler(true, emptyList())
        val transformed = BeskjedTransformer.toInternal(dummyNokkel, beskjedUtenPrefererteKanaler)
        transformed.prefererteKanaler.shouldBeEmpty()
    }

    @Test
    fun `should set forstBehandlet to behandlet if not null`() {
        val tidspunkt = LocalDateTime.now()
        val behandlet = tidspunkt.minusSeconds(10)

        val beskjed = AvroBeskjedObjectMother.createBeskjedWithTidspunktAndBehandlet(tidspunkt.toEpochMilli(), behandlet.toEpochMilli())

        val transformed = BeskjedTransformer.toInternal(dummyNokkel, beskjed)

        transformed.eventTidspunkt.truncatedTo(MILLIS) shouldBe tidspunkt.truncatedTo(MILLIS)
        transformed.forstBehandlet.truncatedTo(MILLIS) shouldBe behandlet.truncatedTo(MILLIS)
    }

    @Test
    fun `should set forstBehandlet to tidspunkt if behandlet is null`() {
        val tidspunkt = LocalDateTime.now()
        val behandlet = null

        val beskjed = AvroBeskjedObjectMother.createBeskjedWithTidspunktAndBehandlet(tidspunkt.toEpochMilli(), behandlet)

        val transformed = BeskjedTransformer.toInternal(dummyNokkel, beskjed)

        transformed.eventTidspunkt.truncatedTo(MILLIS)  shouldBe tidspunkt.truncatedTo(MILLIS)
        transformed.forstBehandlet.truncatedTo(MILLIS)  shouldBe tidspunkt.truncatedTo(MILLIS)
    }

    @Test
    fun `should attempt to fix and set forstBehandlet if behandlet is null and tidspunkt appears truncated`() {
        val tidspunkt = LocalDateTime.now()
        val behandlet = null

        val truncatedTidspunkt = tidspunkt.toEpochSecond(ZoneOffset.UTC)

        val beskjed = AvroBeskjedObjectMother.createBeskjedWithTidspunktAndBehandlet(truncatedTidspunkt, behandlet)

        val transformed = BeskjedTransformer.toInternal(dummyNokkel, beskjed)

        transformed.eventTidspunkt.toEpochMilli() shouldBe truncatedTidspunkt
        transformed.forstBehandlet.truncatedTo(SECONDS) shouldBe tidspunkt.truncatedTo(SECONDS)
    }
}
