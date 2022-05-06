package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.toEpochMilli
import no.nav.personbruker.dittnav.eventaggregator.done.schema.AvroDoneObjectMother
import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class DoneTransformerTest {

    private val eventId = 123
    private val dummyNokkel = createNokkel(eventId)

    @Test
    fun `should transform form external to internal`() {

        val original = AvroDoneObjectMother.createDone()
        val transformed = DoneTransformer.toInternal(dummyNokkel, original)

        transformed.systembruker `should be equal to` dummyNokkel.getSystembruker()
        transformed.fodselsnummer `should be equal to` dummyNokkel.getFodselsnummer()
        transformed.grupperingsId `should be equal to` dummyNokkel.getGrupperingsId()
        transformed.eventId `should be equal to` dummyNokkel.getEventId()
        transformed.namespace `should be equal to` dummyNokkel.getNamespace()
        transformed.appnavn `should be equal to` dummyNokkel.getAppnavn()

        val transformedEventTidspunktAsLong = transformed.eventTidspunkt.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        transformedEventTidspunktAsLong `should be equal to` original.getTidspunkt()
    }

    @Test
    fun `should set forstBehandlet to behandlet if not null`() {
        val tidspunkt = LocalDateTime.now()
        val behandlet = tidspunkt.minusSeconds(10)

        val done = AvroDoneObjectMother.createDoneWithTidspunktAndBehandlet(tidspunkt.toEpochMilli(), behandlet.toEpochMilli())

        val transformed = DoneTransformer.toInternal(dummyNokkel, done)

        transformed.eventTidspunkt.truncatedTo(ChronoUnit.MILLIS) `should be equal to` tidspunkt.truncatedTo(ChronoUnit.MILLIS)
        transformed.forstBehandlet.truncatedTo(ChronoUnit.MILLIS) `should be equal to` behandlet.truncatedTo(ChronoUnit.MILLIS)
    }

    @Test
    fun `should set forstBehandlet to tidspunkt if behandlet is null`() {
        val tidspunkt = LocalDateTime.now()
        val behandlet = null

        val done = AvroDoneObjectMother.createDoneWithTidspunktAndBehandlet(tidspunkt.toEpochMilli(), behandlet)

        val transformed = DoneTransformer.toInternal(dummyNokkel, done)

        transformed.eventTidspunkt.truncatedTo(ChronoUnit.MILLIS)  `should be equal to` tidspunkt.truncatedTo(ChronoUnit.MILLIS)
        transformed.forstBehandlet.truncatedTo(ChronoUnit.MILLIS)  `should be equal to` tidspunkt.truncatedTo(ChronoUnit.MILLIS)
    }

    @Test
    fun `should attempt to fix and set forstBehandlet if behandlet is null and tidspunkt appears truncated`() {
        val tidspunkt = LocalDateTime.now()
        val behandlet = null

        val truncatedTidspunkt = tidspunkt.toEpochSecond(ZoneOffset.UTC)

        val done = AvroDoneObjectMother.createDoneWithTidspunktAndBehandlet(truncatedTidspunkt, behandlet)

        val transformed = DoneTransformer.toInternal(dummyNokkel, done)

        transformed.eventTidspunkt.toEpochMilli() `should be equal to` truncatedTidspunkt
        transformed.forstBehandlet.truncatedTo(ChronoUnit.SECONDS) `should be equal to` tidspunkt.truncatedTo(ChronoUnit.SECONDS)
    }

}
