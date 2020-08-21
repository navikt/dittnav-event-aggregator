package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.FieldValidationException
import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import org.amshove.kluent.*
import org.junit.jupiter.api.Test
import java.time.ZoneId

class StatusoppdateringTransformerTest {

    private val dummyNokkel = createNokkel(1)

    @Test
    fun `should transform form external to internal`() {
        val eventId = 1
        val original = AvroStatusoppdateringObjectMother.createStatusoppdatering(eventId)
        val nokkel = createNokkel(eventId)

        val transformed = StatusoppdateringTransformer.toInternal(nokkel, original)

        transformed.fodselsnummer `should be equal to` original.getFodselsnummer()
        transformed.grupperingsId `should be equal to` original.getGrupperingsId()
        transformed.eventId `should be equal to` nokkel.getEventId()
        transformed.link `should be equal to` original.getLink()
        transformed.systembruker `should be equal to` nokkel.getSystembruker()
        transformed.sikkerhetsnivaa `should be equal to` original.getSikkerhetsnivaa()
        transformed.statusGlobal `should be equal to` original.getStatusGlobal()
        transformed.statusIntern?.`should be equal to`(original.getStatusIntern())
        transformed.sakstema `should be equal to` original.getSakstema()

        val transformedEventTidspunktAsLong = transformed.eventTidspunkt.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
        transformedEventTidspunktAsLong `should be equal to` original.getTidspunkt()

        transformed.sistOppdatert.`should not be null`()
        transformed.id.`should be null`()
    }

    @Test
    fun `should throw FieldValidationException when fodselsnummer is empty`() {
        val fodselsnummerEmpty = ""
        val event = AvroStatusoppdateringObjectMother.createStatusoppdateringWithFodselsnummer(fodselsnummerEmpty)

        invoking {
            runBlocking {
                StatusoppdateringTransformer.toInternal(dummyNokkel, event)
            }
        } `should throw` FieldValidationException::class
    }

    @Test
    fun `should throw FieldValidationException if sakstema field is too long`() {
        val tooLongSakstema = "A".repeat(51)
        val event = AvroStatusoppdateringObjectMother.createStatusoppdateringWithSakstema(tooLongSakstema)

        invoking {
            runBlocking {
                StatusoppdateringTransformer.toInternal(dummyNokkel, event)
            }
        } `should throw` FieldValidationException::class
    }

    @Test
    fun `should allow sakstema length up to the limit`() {
        val sakstemaWithMaxAllowedLength = "S".repeat(50)
        val event = AvroStatusoppdateringObjectMother.createStatusoppdateringWithSakstema(sakstemaWithMaxAllowedLength)

        runBlocking {
            StatusoppdateringTransformer.toInternal(dummyNokkel, event)
        }
    }

    @Test
    fun `should not allow empty sakstema`() {
        val emptySakstema = ""
        val event = AvroStatusoppdateringObjectMother.createStatusoppdateringWithSakstema(emptySakstema)

        invoking {
            runBlocking {
                StatusoppdateringTransformer.toInternal(dummyNokkel, event)
            }
        } `should throw` FieldValidationException::class
    }

    @Test
    fun `should allow statusIntern to be null`() {
        val statusoppdateringUtenSynligTilSatt = AvroStatusoppdateringObjectMother.createStatusoppdateringWithStatusIntern(null)

        val transformed = StatusoppdateringTransformer.toInternal(dummyNokkel, statusoppdateringUtenSynligTilSatt)

        transformed.statusIntern.`should be null`()
    }

    @Test
    fun `should throw FieldValidationException if statusIntern field is too long`() {
        val tooLongStatusIntern = "S".repeat(101)
        val event = AvroStatusoppdateringObjectMother.createStatusoppdateringWithStatusIntern(tooLongStatusIntern)

        invoking {
            runBlocking {
                StatusoppdateringTransformer.toInternal(dummyNokkel, event)
            }
        } `should throw` FieldValidationException::class
    }

    @Test
    fun `should allow statusIntern length up to the limit`() {
        val statusInternWithMaxAllowedLength = "S".repeat(100)
        val event = AvroStatusoppdateringObjectMother.createStatusoppdateringWithStatusIntern(statusInternWithMaxAllowedLength)

        runBlocking {
            StatusoppdateringTransformer.toInternal(dummyNokkel, event)
        }
    }

    @Test
    fun `should throw FieldValidationException if statusGlobal is invalid`() {
        val invalidStatusGlobal = "dummyStatusGlobal"
        val event = AvroStatusoppdateringObjectMother.createStatusoppdateringWithStatusGlobal(invalidStatusGlobal)

        invoking {
            runBlocking {
                StatusoppdateringTransformer.toInternal(dummyNokkel, event)
            }
        } `should throw` FieldValidationException::class
    }

    @Test
    fun `should allow statusGlobal if field is valid`() {
        val validStatusGlobal = "SENDT"
        val event = AvroStatusoppdateringObjectMother.createStatusoppdateringWithSakstema(validStatusGlobal)

        runBlocking {
            StatusoppdateringTransformer.toInternal(dummyNokkel, event)
        }
    }
}
