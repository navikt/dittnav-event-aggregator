package no.nav.personbruker.dittnav.eventaggregator.beskjed

import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.builders.domain.PreferertKanal
import no.nav.brukernotifikasjon.schemas.builders.exception.FieldValidationException
import no.nav.personbruker.dittnav.eventaggregator.common.`with message containing`
import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import org.amshove.kluent.*
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

        transformed.fodselsnummer `should be equal to` original.getFodselsnummer()
        transformed.grupperingsId `should be equal to` original.getGrupperingsId()
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
    fun `should throw FieldValidationException when fodselsnummer is empty`() {
        val fodselsnummerEmpty = ""
        val event = AvroBeskjedObjectMother.createBeskjedWithFodselsnummer(fodselsnummerEmpty)

        invoking {
            runBlocking {
                BeskjedTransformer.toInternal(dummyNokkel, event)
            }
        } `should throw` FieldValidationException::class `with message containing` "fodselsnummer"
    }

    @Test
    fun `should throw FieldValidationException if text field is too long`() {
        val tooLongText = "A".repeat(301)
        val event = AvroBeskjedObjectMother.createBeskjedWithText(tooLongText)

        invoking {
            runBlocking {
                BeskjedTransformer.toInternal(dummyNokkel, event)
            }
        } `should throw` FieldValidationException::class `with message containing` "tekst"
    }

    @Test
    fun `should allow text length up to the limit`() {
        val textWithMaxAllowedLength = "B".repeat(300)
        val event = AvroBeskjedObjectMother.createBeskjedWithText(textWithMaxAllowedLength)

        runBlocking {
            BeskjedTransformer.toInternal(dummyNokkel, event)
        }
    }

    @Test
    fun `should not allow empty text`() {
        val emptyText = ""
        val event = AvroBeskjedObjectMother.createBeskjedWithText(emptyText)

        invoking {
            runBlocking {
                BeskjedTransformer.toInternal(dummyNokkel, event)
            }
        } `should throw` FieldValidationException::class `with message containing` "tekst"
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
    fun `should not allow prefererteKanaler if eksternVarsling is false`() {
        val beskjedUtenEksternVarsling = AvroBeskjedObjectMother.createBeskjedWithEksternVarslingAndPrefererteKanaler(false, listOf(PreferertKanal.EPOST.toString()))
        invoking {
            BeskjedTransformer.toInternal(dummyNokkel, beskjedUtenEksternVarsling)
        } `should throw` FieldValidationException::class `with message containing` "prefererteKanaler"
    }
}
