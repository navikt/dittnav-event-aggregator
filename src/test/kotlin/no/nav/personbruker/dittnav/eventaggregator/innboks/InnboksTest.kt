package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.FieldValidationException
import org.amshove.kluent.`should contain`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.invoking
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId

class InnboksTest {

    private val validProdusent = "DittNAV"
    private val validFodselsnummer = "123"
    private val eventTidspunkt = LocalDateTime.now(ZoneId.of("UTC"))
    private val sistOppdatert = LocalDateTime.now(ZoneId.of("UTC"))
    private val validEventId = "b-2"
    private val validGrupperingsId = "65432"
    private val validTekst = "Dette er et innboks-event til brukeren"
    private val validLink = "https://www.nav.no/systemX/"
    private val validSikkerhetsnivaa = 4

    @Test
    fun `skal returnere maskerte data fra toString-metoden`() {
        val innboks = InnboksObjectMother.giveMeAktivInnboks("dummyEventId", "123")
        val innboksAsString = innboks.toString()
        innboksAsString `should contain` "fodselsnummer=***"
        innboksAsString `should contain` "tekst=***"
        innboksAsString `should contain` "link=***"
    }

    @Test
    fun `do not allow too long produsent`() {
        val tooLongProdusent = "P".repeat(101)
        invoking {
            Innboks(
                    produsent = tooLongProdusent,
                    eventTidspunkt = eventTidspunkt,
                    fodselsnummer = validFodselsnummer,
                    eventId = validEventId,
                    grupperingsId = validGrupperingsId,
                    tekst = validTekst,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    aktiv = true
            )
        } `should throw` FieldValidationException::class
    }

    @Test
    fun `do not allow too long fodselsnummer`() {
        val tooLongFnr = "1".repeat(12)
        invoking {
            Innboks(
                    produsent = validProdusent,
                    eventTidspunkt = eventTidspunkt,
                    fodselsnummer = tooLongFnr,
                    eventId = validEventId,
                    grupperingsId = validGrupperingsId,
                    tekst = validTekst,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    aktiv = true
            )
        } `should throw` FieldValidationException::class
    }

    @Test
    fun `do not allow too long eventid`() {
        val tooLongEventId = "E".repeat(51)
        invoking {
            Innboks(
                    produsent = validProdusent,
                    eventTidspunkt = eventTidspunkt,
                    fodselsnummer = validFodselsnummer,
                    eventId = tooLongEventId,
                    grupperingsId = validGrupperingsId,
                    tekst = validTekst,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    aktiv = true)
        } `should throw` FieldValidationException::class
    }

    @Test
    fun `do not allow too long grupperingsId`() {
        val tooLongGrupperingsId = "G".repeat(101)
        invoking {
            Innboks(
                    produsent = validProdusent,
                    eventTidspunkt = eventTidspunkt,
                    fodselsnummer = validFodselsnummer,
                    eventId = validEventId,
                    grupperingsId = tooLongGrupperingsId,
                    tekst = validTekst,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    aktiv = true)
        } `should throw` FieldValidationException::class
    }

    @Test
    fun `do not allow too long tekst`() {
        val tooLongText = "T".repeat(501)
        invoking {
            Innboks(
                    produsent = validProdusent,
                    eventTidspunkt = eventTidspunkt,
                    fodselsnummer = validFodselsnummer,
                    eventId = validEventId,
                    grupperingsId = validGrupperingsId,
                    tekst = tooLongText,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    aktiv = true)
        } `should throw` FieldValidationException::class
    }

    @Test
    fun `do not allow too long link`() {
        val tooLongLink = "L".repeat(201)
        invoking {
            Innboks(

                    produsent = validProdusent,
                    eventTidspunkt = eventTidspunkt,
                    fodselsnummer = validFodselsnummer,
                    eventId = validEventId,
                    grupperingsId = validGrupperingsId,
                    tekst = validTekst,
                    link = tooLongLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    aktiv = true)
        } `should throw` FieldValidationException::class
    }

    @Test
    fun `do not allow invalid sikkerhetsnivaa`() {
        val invalidSikkerhetsnivaa = 2
        invoking {
            Innboks(
                    produsent = validProdusent,
                    eventTidspunkt = eventTidspunkt,
                    fodselsnummer = validFodselsnummer,
                    eventId = validEventId,
                    grupperingsId = validGrupperingsId,
                    tekst = validTekst,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = invalidSikkerhetsnivaa,
                    aktiv = true)
        } `should throw` FieldValidationException::class
    }


}
