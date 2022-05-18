package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import no.nav.personbruker.dittnav.eventaggregator.common.toEpochMilli
import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class StatusoppdateringTransformerTest {

    private val eventId = 1
    private val dummyNokkel = createNokkel(eventId)

    @Test
    fun `should transform form external to internal`() {
        val original = AvroStatusoppdateringObjectMother.createStatusoppdatering()

        val transformed = StatusoppdateringTransformer.toInternal(dummyNokkel, original)

        transformed.fodselsnummer shouldBe dummyNokkel.getFodselsnummer()
        transformed.grupperingsId shouldBe dummyNokkel.getGrupperingsId()
        transformed.eventId shouldBe dummyNokkel.getEventId()
        transformed.link shouldBe original.getLink()
        transformed.systembruker shouldBe dummyNokkel.getSystembruker()
        transformed.sikkerhetsnivaa shouldBe original.getSikkerhetsnivaa()
        transformed.statusGlobal shouldBe original.getStatusGlobal()
        transformed.statusIntern?.shouldBe(original.getStatusIntern())
        transformed.sakstema shouldBe original.getSakstema()
        transformed.namespace shouldBe dummyNokkel.getNamespace()
        transformed.appnavn shouldBe dummyNokkel.getAppnavn()

        val transformedEventTidspunktAsLong = transformed.eventTidspunkt.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        transformedEventTidspunktAsLong shouldBe original.getTidspunkt()

        transformed.sistOppdatert.shouldNotBeNull()
        transformed.id.shouldBeNull()
    }

    @Test
    fun `should set forstBehandlet to behandlet if not null`() {
        val tidspunkt = LocalDateTime.now()
        val behandlet = tidspunkt.minusSeconds(10)

        val statusoppdatering = AvroStatusoppdateringObjectMother.createStatusoppdateringWithTidspunktAndBehandlet(tidspunkt.toEpochMilli(), behandlet.toEpochMilli())

        val transformed = StatusoppdateringTransformer.toInternal(dummyNokkel, statusoppdatering)

        transformed.eventTidspunkt.truncatedTo(ChronoUnit.MILLIS) shouldBe tidspunkt.truncatedTo(ChronoUnit.MILLIS)
        transformed.forstBehandlet.truncatedTo(ChronoUnit.MILLIS) shouldBe behandlet.truncatedTo(ChronoUnit.MILLIS)
    }

    @Test
    fun `should set forstBehandlet to tidspunkt if behandlet is null`() {
        val tidspunkt = LocalDateTime.now()
        val behandlet = null

        val statusoppdatering = AvroStatusoppdateringObjectMother.createStatusoppdateringWithTidspunktAndBehandlet(tidspunkt.toEpochMilli(), behandlet)

        val transformed = StatusoppdateringTransformer.toInternal(dummyNokkel, statusoppdatering)

        transformed.eventTidspunkt.truncatedTo(ChronoUnit.MILLIS)  shouldBe tidspunkt.truncatedTo(ChronoUnit.MILLIS)
        transformed.forstBehandlet.truncatedTo(ChronoUnit.MILLIS)  shouldBe tidspunkt.truncatedTo(ChronoUnit.MILLIS)
    }

    @Test
    fun `should attempt to fix and set forstBehandlet if behandlet is null and tidspunkt appears truncated`() {
        val tidspunkt = LocalDateTime.now()
        val behandlet = null

        val truncatedTidspunkt = tidspunkt.toEpochSecond(ZoneOffset.UTC)

        val statusoppdatering = AvroStatusoppdateringObjectMother.createStatusoppdateringWithTidspunktAndBehandlet(truncatedTidspunkt, behandlet)

        val transformed = StatusoppdateringTransformer.toInternal(dummyNokkel, statusoppdatering)

        transformed.eventTidspunkt.toEpochMilli() shouldBe truncatedTidspunkt
        transformed.forstBehandlet.truncatedTo(ChronoUnit.SECONDS) shouldBe tidspunkt.truncatedTo(ChronoUnit.SECONDS)
    }
}
