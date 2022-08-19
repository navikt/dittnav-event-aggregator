package no.nav.personbruker.dittnav.eventaggregator.done

import io.kotest.matchers.shouldBe
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowTruncatedToMillis
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.toEpochMilli
import no.nav.personbruker.dittnav.eventaggregator.done.schema.AvroDoneObjectMother
import no.nav.personbruker.dittnav.eventaggregator.nokkel.AvroNokkelInternObjectMother
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class DoneTransformerTest {

    private val eventId = 123
    private val dummyNokkel = AvroNokkelInternObjectMother.createNokkelWithEventId(eventId)

    @Test
    fun `should transform form external to internal`() {

        val original = AvroDoneObjectMother.createDone()
        val transformed = DoneTransformer.toInternal(dummyNokkel, original)

        transformed.systembruker shouldBe dummyNokkel.getSystembruker()
        transformed.fodselsnummer shouldBe dummyNokkel.getFodselsnummer()
        transformed.grupperingsId shouldBe dummyNokkel.getGrupperingsId()
        transformed.eventId shouldBe dummyNokkel.getEventId()
        transformed.namespace shouldBe dummyNokkel.getNamespace()
        transformed.appnavn shouldBe dummyNokkel.getAppnavn()

        val transformedEventTidspunktAsLong = transformed.eventTidspunkt.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        transformedEventTidspunktAsLong shouldBe original.getTidspunkt()
    }

    @Test
    fun `should set forstBehandlet to behandlet if not null`() {
        val tidspunkt = nowTruncatedToMillis()
        val behandlet = tidspunkt.minusSeconds(10)

        val done = AvroDoneObjectMother.createDoneWithTidspunktAndBehandlet(tidspunkt.toEpochMilli(), behandlet.toEpochMilli())

        val transformed = DoneTransformer.toInternal(dummyNokkel, done)

        transformed.eventTidspunkt shouldBe tidspunkt
        transformed.forstBehandlet shouldBe behandlet
    }

    @Test
    fun `should set forstBehandlet to tidspunkt if behandlet is null`() {
        val tidspunkt = nowTruncatedToMillis()
        val behandlet = null

        val done = AvroDoneObjectMother.createDoneWithTidspunktAndBehandlet(tidspunkt.toEpochMilli(), behandlet)

        val transformed = DoneTransformer.toInternal(dummyNokkel, done)

        transformed.eventTidspunkt  shouldBe tidspunkt
        transformed.forstBehandlet  shouldBe tidspunkt
    }

    @Test
    fun `should attempt to fix and set forstBehandlet if behandlet is null and tidspunkt appears truncated`() {
        val tidspunkt = nowTruncatedToMillis()
        val behandlet = null

        val truncatedTidspunkt = tidspunkt.toEpochSecond(ZoneOffset.UTC)

        val done = AvroDoneObjectMother.createDoneWithTidspunktAndBehandlet(truncatedTidspunkt, behandlet)

        val transformed = DoneTransformer.toInternal(dummyNokkel, done)

        transformed.eventTidspunkt.toEpochMilli() shouldBe truncatedTidspunkt
        transformed.forstBehandlet.truncatedTo(ChronoUnit.SECONDS) shouldBe tidspunkt.truncatedTo(ChronoUnit.SECONDS)
    }

}
