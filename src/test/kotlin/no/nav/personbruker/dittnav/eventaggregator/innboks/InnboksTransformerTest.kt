package no.nav.personbruker.dittnav.eventaggregator.innboks

import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.builders.domain.PreferertKanal
import no.nav.brukernotifikasjon.schemas.builders.exception.FieldValidationException
import no.nav.personbruker.dittnav.eventaggregator.common.`with message containing`
import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import org.amshove.kluent.*
import org.junit.jupiter.api.Test
import java.time.ZoneId

class InnboksTransformerTest {

    @Test
    fun `should transform external to internal`() {
        val eventId = 123
        val external = AvroInnboksObjectMother.createInnboks(eventId)
        val nokkel = createNokkel(eventId)

        val internal = InnboksTransformer.toInternal(nokkel, external)

        internal.fodselsnummer `should be equal to` external.getFodselsnummer()
        internal.grupperingsId `should be equal to` external.getGrupperingsId()
        internal.eventId `should be equal to` nokkel.getEventId()
        internal.link `should be equal to` external.getLink()
        internal.tekst `should be equal to` external.getTekst()
        internal.systembruker `should be equal to` nokkel.getSystembruker()
        internal.sikkerhetsnivaa `should be equal to` external.getSikkerhetsnivaa()
        internal.eksternVarsling `should be equal to` external.getEksternVarsling()
        internal.prefererteKanaler `should be equal to` external.getPrefererteKanaler()

        val transformedEventTidspunktAsLong = internal.eventTidspunkt.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        transformedEventTidspunktAsLong `should be equal to` external.getTidspunkt()

        internal.aktiv `should be` true
        internal.sistOppdatert.`should not be null`()
        internal.id.`should be null`()
    }

    @Test
    fun `should throw FieldValidationException when fodselsnummer is empty`() {
        val fodselsnummer = ""
        val eventId = 123
        val event = AvroInnboksObjectMother.createInnboks(eventId, fodselsnummer)
        val nokkel = createNokkel(eventId)

        invoking {
            runBlocking {
                InnboksTransformer.toInternal(nokkel, event)
            }
        } `should throw` FieldValidationException::class
    }

    @Test
    fun `should allow prefererteKanaler to be empty`() {
        val innboksUtenPrefererteKanaler = AvroInnboksObjectMother.createInnboksWithEksternVarslingAndPrefererteKanaler(true, emptyList())
        val nokkel = createNokkel(123)
        val transformed = InnboksTransformer.toInternal(nokkel, innboksUtenPrefererteKanaler)
        transformed.prefererteKanaler.`should be empty`()
    }

    @Test
    fun `should not allow prefererteKanaler if eksternVarsling is false`() {
        val innboksUtenEksternVarsling = AvroInnboksObjectMother.createInnboksWithEksternVarslingAndPrefererteKanaler(false, listOf(PreferertKanal.EPOST.toString()))
        val nokkel = createNokkel(123)
        invoking {
            InnboksTransformer.toInternal(nokkel, innboksUtenEksternVarsling)
        } `should throw` FieldValidationException::class `with message containing` "prefererteKanaler"
    }
}
