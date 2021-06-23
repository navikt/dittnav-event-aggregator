package no.nav.personbruker.dittnav.eventaggregator.oppgave

import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.builders.domain.PreferertKanal
import no.nav.brukernotifikasjon.schemas.builders.exception.FieldValidationException
import no.nav.personbruker.dittnav.eventaggregator.common.`with message containing`
import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import org.amshove.kluent.*
import org.junit.jupiter.api.Test
import java.time.ZoneId

class OppgaveTransformerTest {

    @Test
    fun `should transform external to internal`() {
        val eventId = 1
        val external = AvroOppgaveObjectMother.createOppgave(eventId)
        val nokkel = createNokkel(eventId)

        val internal = OppgaveTransformer.toInternal(nokkel, external)

        internal.fodselsnummer `should be equal to` external.getFodselsnummer()
        internal.grupperingsId `should be equal to` external.getGrupperingsId()
        internal.eventId `should be equal to` nokkel.getEventId()
        internal.link `should be equal to` external.getLink()
        internal.tekst `should be equal to` external.getTekst()
        internal.systembruker `should be equal to` nokkel.getSystembruker()
        internal.sikkerhetsnivaa `should be equal to` external.getSikkerhetsnivaa()

        val transformedEventTidspunktAsLong = internal.eventTidspunkt.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        transformedEventTidspunktAsLong `should be equal to` external.getTidspunkt()

        internal.aktiv `should be equal to` true
        internal.eksternVarsling `should be equal to` true
        internal.prefererteKanaler `should be equal to` external.getPrefererteKanaler()
        internal.sistOppdatert.`should not be null`()
        internal.id.`should be null`()
    }

    @Test
    fun `should throw FieldValidationException when fodselsnummer is empty`() {
        val fodselsnummer = ""
        val eventId = 1
        val event = AvroOppgaveObjectMother.createOppgave(eventId, fodselsnummer)
        val nokkel = createNokkel(eventId)

        invoking {
            runBlocking {
                OppgaveTransformer.toInternal(nokkel, event)
            }
        } `should throw` FieldValidationException::class `with message containing` "fodselsnummer"
    }

    @Test
    fun `should allow prefererteKanaler to be empty`() {
        val eventId = 1
        val nokkel = createNokkel(eventId)
        val eventUtenPrefererteKanaler = AvroOppgaveObjectMother.createOppgaveWithEksternVarslingAndPrefererteKanaler(true, emptyList())
        val internal = OppgaveTransformer.toInternal(nokkel, eventUtenPrefererteKanaler)
        internal.prefererteKanaler.`should be empty`()
    }

    @Test
    fun `should not allow prefererteKanaler if eksternVarsling is false`() {
        val eventId = 1
        val nokkel = createNokkel(eventId)
        val eventUtenEksternVarsling = AvroOppgaveObjectMother.createOppgaveWithEksternVarslingAndPrefererteKanaler(false, listOf(PreferertKanal.EPOST.toString()))
        invoking {
            OppgaveTransformer.toInternal(nokkel, eventUtenEksternVarsling)
        } `should throw` FieldValidationException::class `with message containing` "prefererteKanaler"
    }
}
