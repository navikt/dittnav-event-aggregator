package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import no.nav.personbruker.dittnav.eventaggregator.common.toEpochMilli
import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should not be null`
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class StatusoppdateringTransformerTest {

    val eventId = 1
    val dummyNokkel = createNokkel(eventId)

    @Test
    fun `should transform form external to internal`() {
        val original = AvroStatusoppdateringObjectMother.createStatusoppdatering()

        val transformed = StatusoppdateringTransformer.toInternal(dummyNokkel, original)

        transformed.fodselsnummer `should be equal to` dummyNokkel.getFodselsnummer()
        transformed.grupperingsId `should be equal to` dummyNokkel.getGrupperingsId()
        transformed.eventId `should be equal to` dummyNokkel.getEventId()
        transformed.link `should be equal to` original.getLink()
        transformed.systembruker `should be equal to` dummyNokkel.getSystembruker()
        transformed.sikkerhetsnivaa `should be equal to` original.getSikkerhetsnivaa()
        transformed.statusGlobal `should be equal to` original.getStatusGlobal()
        transformed.statusIntern?.`should be equal to`(original.getStatusIntern())
        transformed.sakstema `should be equal to` original.getSakstema()
        transformed.namespace `should be equal to` dummyNokkel.getNamespace()
        transformed.appnavn `should be equal to` dummyNokkel.getAppnavn()

        val transformedEventTidspunktAsLong = transformed.eventTidspunkt.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        transformedEventTidspunktAsLong `should be equal to` original.getTidspunkt()

        transformed.sistOppdatert.`should not be null`()
        transformed.id.`should be null`()
    }

    @Test
    fun `should set forstBehandlet to behandlet if not null`() {
        val tidspunkt = LocalDateTime.now()
        val behandlet = tidspunkt.minusSeconds(10)

        val statusoppdatering = AvroStatusoppdateringObjectMother.createStatusoppdateringWithTidspunktAndBehandlet(tidspunkt.toEpochMilli(), behandlet.toEpochMilli())

        val transformed = StatusoppdateringTransformer.toInternal(dummyNokkel, statusoppdatering)

        transformed.eventTidspunkt.truncatedTo(ChronoUnit.MILLIS) `should be equal to` tidspunkt.truncatedTo(ChronoUnit.MILLIS)
        transformed.forstBehandlet.truncatedTo(ChronoUnit.MILLIS) `should be equal to` behandlet.truncatedTo(ChronoUnit.MILLIS)
    }

    @Test
    fun `should set forstBehandlet to tidspunkt if behandlet is null`() {
        val tidspunkt = LocalDateTime.now()
        val behandlet = null

        val statusoppdatering = AvroStatusoppdateringObjectMother.createStatusoppdateringWithTidspunktAndBehandlet(tidspunkt.toEpochMilli(), behandlet)

        val transformed = StatusoppdateringTransformer.toInternal(dummyNokkel, statusoppdatering)

        transformed.eventTidspunkt.truncatedTo(ChronoUnit.MILLIS)  `should be equal to` tidspunkt.truncatedTo(ChronoUnit.MILLIS)
        transformed.forstBehandlet.truncatedTo(ChronoUnit.MILLIS)  `should be equal to` tidspunkt.truncatedTo(ChronoUnit.MILLIS)
    }

    @Test
    fun `should attempt to fix and set forstBehandlet if behandlet is null and tidspunkt appears truncated`() {
        val tidspunkt = LocalDateTime.now()
        val behandlet = null

        val truncatedTidspunkt = tidspunkt.toEpochSecond(ZoneOffset.UTC)

        val statusoppdatering = AvroStatusoppdateringObjectMother.createStatusoppdateringWithTidspunktAndBehandlet(truncatedTidspunkt, behandlet)

        val transformed = StatusoppdateringTransformer.toInternal(dummyNokkel, statusoppdatering)

        transformed.eventTidspunkt.toEpochMilli() `should be equal to` truncatedTidspunkt
        transformed.forstBehandlet.truncatedTo(ChronoUnit.SECONDS) `should be equal to` tidspunkt.truncatedTo(ChronoUnit.SECONDS)
    }
}
