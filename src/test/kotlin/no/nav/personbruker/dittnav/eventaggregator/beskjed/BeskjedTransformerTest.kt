package no.nav.personbruker.dittnav.eventaggregator.beskjed

import org.amshove.kluent.`should be empty`

import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should not be null`
import org.junit.jupiter.api.Test
import java.time.ZoneId

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

}
