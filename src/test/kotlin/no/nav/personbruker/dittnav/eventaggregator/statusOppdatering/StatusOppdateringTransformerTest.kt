package no.nav.personbruker.dittnav.eventaggregator.statusOppdatering

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.FieldValidationException
import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import org.amshove.kluent.*
import org.junit.jupiter.api.Test
import java.time.ZoneId

class StatusOppdateringTransformerTest {

    private val dummyNokkel = createNokkel(1)

    @Test
    fun `should transform form external to internal`() {
        val eventId = 1
        val original = AvroStatusOppdateringObjectMother.createStatusOppdatering(eventId)
        val nokkel = createNokkel(eventId)

        val transformed = StatusOppdateringTransformer.toInternal(nokkel, original)

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
        val event = AvroStatusOppdateringObjectMother.createStatusOppdateringWithFodselsnummer(fodselsnummerEmpty)

        invoking {
            runBlocking {
                StatusOppdateringTransformer.toInternal(dummyNokkel, event)
            }
        } `should throw` FieldValidationException::class
    }

    @Test
    fun `should throw FieldValidationException if sakstema field is too long`() {
        val tooLongSakstema = "A".repeat(51)
        val event = AvroStatusOppdateringObjectMother.createStatusOppdateringWithSakstema(tooLongSakstema)

        invoking {
            runBlocking {
                StatusOppdateringTransformer.toInternal(dummyNokkel, event)
            }
        } `should throw` FieldValidationException::class
    }

    @Test
    fun `should allow sakstema length up to the limit`() {
        val sakstemaWithMaxAllowedLength = "S".repeat(50)
        val event = AvroStatusOppdateringObjectMother.createStatusOppdateringWithSakstema(sakstemaWithMaxAllowedLength)

        runBlocking {
            StatusOppdateringTransformer.toInternal(dummyNokkel, event)
        }
    }

    @Test
    fun `should not allow empty sakstema`() {
        val emptySakstema = ""
        val event = AvroStatusOppdateringObjectMother.createStatusOppdateringWithSakstema(emptySakstema)

        invoking {
            runBlocking {
                StatusOppdateringTransformer.toInternal(dummyNokkel, event)
            }
        } `should throw` FieldValidationException::class
    }

    @Test
    fun `should allow statusIntern to be null`() {
        val statusOppdateringUtenSynligTilSatt = AvroStatusOppdateringObjectMother.createStatusOppdateringWithStatusIntern(null)

        val transformed = StatusOppdateringTransformer.toInternal(dummyNokkel, statusOppdateringUtenSynligTilSatt)

        transformed.statusIntern.`should be null`()
    }

    @Test
    fun `should throw FieldValidationException if statusIntern field is too long`() {
        val tooLongStatusIntern = "S".repeat(101)
        val event = AvroStatusOppdateringObjectMother.createStatusOppdateringWithStatusIntern(tooLongStatusIntern)

        invoking {
            runBlocking {
                StatusOppdateringTransformer.toInternal(dummyNokkel, event)
            }
        } `should throw` FieldValidationException::class
    }

    @Test
    fun `should allow statusIntern length up to the limit`() {
        val statusInternWithMaxAllowedLength = "S".repeat(100)
        val event = AvroStatusOppdateringObjectMother.createStatusOppdateringWithStatusIntern(statusInternWithMaxAllowedLength)

        runBlocking {
            StatusOppdateringTransformer.toInternal(dummyNokkel, event)
        }
    }

    @Test
    fun `should throw FieldValidationException if statusGlobal is invalid`() {
        val invalidStatusGlobal = "dummyStatusGlobal"
        val event = AvroStatusOppdateringObjectMother.createStatusOppdateringWithStatusGlobal(invalidStatusGlobal)

        invoking {
            runBlocking {
                StatusOppdateringTransformer.toInternal(dummyNokkel, event)
            }
        } `should throw` FieldValidationException::class
    }

    @Test
    fun `should allow statusGlobal if field is valid`() {
        val validStatusGlobal = "SENDT"
        val event = AvroStatusOppdateringObjectMother.createStatusOppdateringWithSakstema(validStatusGlobal)

        runBlocking {
            StatusOppdateringTransformer.toInternal(dummyNokkel, event)
        }
    }
}
