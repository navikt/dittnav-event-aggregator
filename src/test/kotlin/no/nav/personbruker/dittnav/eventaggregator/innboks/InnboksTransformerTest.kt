package no.nav.personbruker.dittnav.eventaggregator.innboks

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import no.nav.personbruker.dittnav.eventaggregator.common.toEpochMilli
import no.nav.personbruker.dittnav.eventaggregator.nokkel.AvroNokkelInternObjectMother
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class InnboksTransformerTest {

    private val eventId = 123
    private val dummyNokkel = AvroNokkelInternObjectMother.createNokkelWithEventId(eventId)

    @Test
    fun `should transform external to internal`() {
        val external = AvroInnboksObjectMother.createInnboks(eventId)

        val internal = InnboksTransformer.toInternal(dummyNokkel, external)

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
        internal.sistOppdatert.shouldNotBeNull()
        internal.id.shouldBeNull()
    }

    @Test
    fun `should set forstBehandlet to behandlet if not null`() {
        val tidspunkt = LocalDateTime.now()
        val behandlet = tidspunkt.minusSeconds(10)

        val innboks = AvroInnboksObjectMother.createInnboksWithTidspunktAndBehandlet(tidspunkt.toEpochMilli(), behandlet.toEpochMilli())

        val transformed = InnboksTransformer.toInternal(dummyNokkel, innboks)

        transformed.eventTidspunkt.truncatedTo(ChronoUnit.MILLIS) shouldBe tidspunkt.truncatedTo(ChronoUnit.MILLIS)
        transformed.forstBehandlet.truncatedTo(ChronoUnit.MILLIS) shouldBe behandlet.truncatedTo(ChronoUnit.MILLIS)
    }

    @Test
    fun `should set forstBehandlet to tidspunkt if behandlet is null`() {
        val tidspunkt = LocalDateTime.now()
        val behandlet = null

        val innboks = AvroInnboksObjectMother.createInnboksWithTidspunktAndBehandlet(tidspunkt.toEpochMilli(), behandlet)

        val transformed = InnboksTransformer.toInternal(dummyNokkel, innboks)

        transformed.eventTidspunkt.truncatedTo(ChronoUnit.MILLIS)  shouldBe tidspunkt.truncatedTo(ChronoUnit.MILLIS)
        transformed.forstBehandlet.truncatedTo(ChronoUnit.MILLIS)  shouldBe tidspunkt.truncatedTo(ChronoUnit.MILLIS)
    }

    @Test
    fun `should attempt to fix and set forstBehandlet if behandlet is null and tidspunkt appears truncated`() {
        val tidspunkt = LocalDateTime.now()
        val behandlet = null

        val truncatedTidspunkt = tidspunkt.toEpochSecond(ZoneOffset.UTC)

        val innboks = AvroInnboksObjectMother.createInnboksWithTidspunktAndBehandlet(truncatedTidspunkt, behandlet)

        val transformed = InnboksTransformer.toInternal(dummyNokkel, innboks)

        transformed.eventTidspunkt.toEpochMilli() shouldBe truncatedTidspunkt
        transformed.forstBehandlet.truncatedTo(ChronoUnit.SECONDS) shouldBe tidspunkt.truncatedTo(ChronoUnit.SECONDS)
    }
}
