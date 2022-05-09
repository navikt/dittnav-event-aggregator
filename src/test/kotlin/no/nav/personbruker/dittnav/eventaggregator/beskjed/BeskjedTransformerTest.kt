package no.nav.personbruker.dittnav.eventaggregator.beskjed

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

        transformed.fodselsnummer `should be equal to` nokkel.getFodselsnummer()
        transformed.grupperingsId `should be equal to` nokkel.getGrupperingsId()
        transformed.eventId `should be equal to` nokkel.getEventId()
        transformed.link `should be equal to` original.getLink()
        transformed.tekst `should be equal to` original.getTekst()
        transformed.systembruker `should be equal to` nokkel.getSystembruker()
        transformed.sikkerhetsnivaa `should be equal to` original.getSikkerhetsnivaa()
        transformed.appnavn `should be equal to` nokkel.getAppnavn()
        transformed.namespace `should be equal to` nokkel.getNamespace()

        val transformedEventTidspunktAsLong = transformed.eventTidspunkt.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        transformedEventTidspunktAsLong `should be equal to` original.getTidspunkt()

        transformed.aktiv `should be equal to` true
        transformed.eksternVarsling `should be equal to` true
        transformed.prefererteKanaler `should be equal to` original.getPrefererteKanaler()
        transformed.sistOppdatert.`should not be null`()
        transformed.id.`should be null`()
    }

    @Test
    fun `should allow synligFremTil to be null`() {
        val beskjedUtenSynligTilSatt = AvroBeskjedObjectMother.createBeskjedWithoutSynligFremTilSatt()

        val transformed = BeskjedTransformer.toInternal(dummyNokkel, beskjedUtenSynligTilSatt)

        transformed.synligFremTil.`should be null`()
    }

    @Test
    fun `should allow prefererteKanaler to be empty`() {
        val beskjedUtenPrefererteKanaler = AvroBeskjedObjectMother.createBeskjedWithEksternVarslingAndPrefererteKanaler(true, emptyList())
        val transformed = BeskjedTransformer.toInternal(dummyNokkel, beskjedUtenPrefererteKanaler)
        transformed.prefererteKanaler.`should be empty`()
    }

    @Test
    fun `should set forstBehandlet to behandlet if not null`() {
        val tidspunkt = LocalDateTime.now()
        val behandlet = tidspunkt.minusSeconds(10)

        val beskjed = AvroBeskjedObjectMother.createBeskjedWithTidspunktAndBehandlet(tidspunkt.toEpochMilli(), behandlet.toEpochMilli())

        val transformed = BeskjedTransformer.toInternal(dummyNokkel, beskjed)

        transformed.eventTidspunkt.truncatedTo(MILLIS) `should be equal to` tidspunkt.truncatedTo(MILLIS)
        transformed.forstBehandlet.truncatedTo(MILLIS) `should be equal to` behandlet.truncatedTo(MILLIS)
    }

    @Test
    fun `should set forstBehandlet to tidspunkt if behandlet is null`() {
        val tidspunkt = LocalDateTime.now()
        val behandlet = null

        val beskjed = AvroBeskjedObjectMother.createBeskjedWithTidspunktAndBehandlet(tidspunkt.toEpochMilli(), behandlet)

        val transformed = BeskjedTransformer.toInternal(dummyNokkel, beskjed)

        transformed.eventTidspunkt.truncatedTo(MILLIS)  `should be equal to` tidspunkt.truncatedTo(MILLIS)
        transformed.forstBehandlet.truncatedTo(MILLIS)  `should be equal to` tidspunkt.truncatedTo(MILLIS)
    }

    @Test
    fun `should attempt to fix and set forstBehandlet if behandlet is null and tidspunkt appears truncated`() {
        val tidspunkt = LocalDateTime.now()
        val behandlet = null

        val truncatedTidspunkt = tidspunkt.toEpochSecond(ZoneOffset.UTC)

        val beskjed = AvroBeskjedObjectMother.createBeskjedWithTidspunktAndBehandlet(truncatedTidspunkt, behandlet)

        val transformed = BeskjedTransformer.toInternal(dummyNokkel, beskjed)

        transformed.eventTidspunkt.toEpochMilli() `should be equal to` truncatedTidspunkt
        transformed.forstBehandlet.truncatedTo(SECONDS) `should be equal to` tidspunkt.truncatedTo(SECONDS)
    }
}
