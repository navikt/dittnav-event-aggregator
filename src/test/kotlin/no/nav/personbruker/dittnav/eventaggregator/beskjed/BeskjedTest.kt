package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.personbruker.dittnav.eventaggregator.common.`with message containing`
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.FieldValidationException
import org.amshove.kluent.`should contain`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.invoking
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.random.Random

class BeskjedTest {

    private val uid = Random.nextInt(1, 100).toString()
    private val validSystembruker = "dummySystembruker"
    private val validFodselsnummer = "123"
    private val eventTidspunkt = LocalDateTime.now(ZoneId.of("UTC"))
    private val synligFremTil = LocalDateTime.now(ZoneId.of("UTC"))
    private val sistOppdatert = LocalDateTime.now(ZoneId.of("UTC"))
    private val validEventId = "b-2"
    private val validGrupperingsId = "65432"
    private val validTekst = "Dette er en beskjed til brukeren"
    private val validLink = "https://www.nav.no/systemX/"
    private val validSikkerhetsnivaa = 4

    @Test
    fun `skal returnere maskerte data fra toString-metoden`() {
        val beskjed = BeskjedObjectMother.giveMeAktivBeskjed("dummyEventId", "123")
        val beskjedAsString = beskjed.toString()
        beskjedAsString `should contain` "fodselsnummer=***"
        beskjedAsString `should contain` "tekst=***"
        beskjedAsString `should contain` "link=***"
    }

    @Test
    fun `do not allow too long systembruker`() {
        val tooLongSystembruker = "P".repeat(101)
        invoking {
            Beskjed(
                    uid = uid,
                    systembruker = tooLongSystembruker,
                    eventTidspunkt = eventTidspunkt,
                    synligFremTil = synligFremTil,
                    fodselsnummer = validFodselsnummer,
                    eventId = validEventId,
                    grupperingsId = validGrupperingsId,
                    tekst = validTekst,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    aktiv = true,
                    eksternVarsling = false)
        } `should throw` FieldValidationException::class `with message containing` "systembruker"
    }

    @Test
    fun `do not allow too long fodselsnummer`() {
        val tooLongFnr = "1".repeat(12)
        invoking {
            Beskjed(
                    uid = uid,
                    systembruker = validSystembruker,
                    eventTidspunkt = eventTidspunkt,
                    synligFremTil = synligFremTil,
                    fodselsnummer = tooLongFnr,
                    eventId = validEventId,
                    grupperingsId = validGrupperingsId,
                    tekst = validTekst,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    aktiv = true,
                    eksternVarsling = false)
        } `should throw` FieldValidationException::class `with message containing` "fodselsnummer"
    }

    @Test
    fun `do not allow too long eventid`() {
        val tooLongEventId = "E".repeat(51)
        invoking {
            Beskjed(
                    uid = uid,
                    systembruker = validSystembruker,
                    eventTidspunkt = eventTidspunkt,
                    synligFremTil = synligFremTil,
                    fodselsnummer = validFodselsnummer,
                    eventId = tooLongEventId,
                    grupperingsId = validGrupperingsId,
                    tekst = validTekst,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    aktiv = true,
                    eksternVarsling = false)
        } `should throw` FieldValidationException::class `with message containing` "eventId"
    }

    @Test
    fun `do not allow too long grupperingsId`() {
        val tooLongGrupperingsId = "G".repeat(101)
        invoking {
            Beskjed(
                    uid = uid,
                    systembruker = validSystembruker,
                    eventTidspunkt = eventTidspunkt,
                    synligFremTil = synligFremTil,
                    fodselsnummer = validFodselsnummer,
                    eventId = validEventId,
                    grupperingsId = tooLongGrupperingsId,
                    tekst = validTekst,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    aktiv = true,
                    eksternVarsling = false)
        } `should throw` FieldValidationException::class `with message containing` "grupperingsId"
    }

    @Test
    fun `do not allow too long tekst`() {
        val tooLongText = "T".repeat(501)
        invoking {
            Beskjed(
                    uid = uid,
                    systembruker = validSystembruker,
                    eventTidspunkt = eventTidspunkt,
                    synligFremTil = synligFremTil,
                    fodselsnummer = validFodselsnummer,
                    eventId = validEventId,
                    grupperingsId = validGrupperingsId,
                    tekst = tooLongText,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    aktiv = true,
                    eksternVarsling = false)
        } `should throw` FieldValidationException::class `with message containing` "tekst"
    }

    @Test
    fun `do not allow too long link`() {
        val tooLongLink = "L".repeat(201)
        invoking {
            Beskjed(
                    uid = uid,
                    systembruker = validSystembruker,
                    eventTidspunkt = eventTidspunkt,
                    synligFremTil = synligFremTil,
                    fodselsnummer = validFodselsnummer,
                    eventId = validEventId,
                    grupperingsId = validGrupperingsId,
                    tekst = validTekst,
                    link = tooLongLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    aktiv = true,
                    eksternVarsling = false)
        } `should throw` FieldValidationException::class `with message containing` "link"
    }

    @Test
    fun `do not allow invalid sikkerhetsnivaa`() {
        val invalidSikkerhetsnivaa = 2
        invoking {
            Beskjed(
                    uid = uid,
                    systembruker = validSystembruker,
                    eventTidspunkt = eventTidspunkt,
                    synligFremTil = synligFremTil,
                    fodselsnummer = validFodselsnummer,
                    eventId = validEventId,
                    grupperingsId = validGrupperingsId,
                    tekst = validTekst,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = invalidSikkerhetsnivaa,
                    aktiv = true,
                    eksternVarsling = false)
        } `should throw` FieldValidationException::class `with message containing` "Sikkerhetsnivaa"
    }

    @Test
    fun `do not allow too long uid`() {
        val tooLongUid = "U".repeat(101)
        invoking {
            Beskjed(
                    uid = tooLongUid,
                    systembruker = validSystembruker,
                    eventTidspunkt = eventTidspunkt,
                    synligFremTil = synligFremTil,
                    fodselsnummer = validFodselsnummer,
                    eventId = validEventId,
                    grupperingsId = validGrupperingsId,
                    tekst = validTekst,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    aktiv = true,
                    eksternVarsling = false)
        } `should throw` FieldValidationException::class `with message containing` "uid"
    }

}
