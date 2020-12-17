package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import no.nav.brukernotifikasjon.schemas.builders.exception.FieldValidationException
import no.nav.personbruker.dittnav.eventaggregator.common.`with message containing`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.invoking
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId

class StatusoppdateringTest {

    private val validSystembruker = "dummySystembruker"
    private val validFodselsnummer = "123"
    private val eventTidspunkt = LocalDateTime.now(ZoneId.of("UTC"))
    private val sistOppdatert = LocalDateTime.now(ZoneId.of("UTC"))
    private val validEventId = "b-2"
    private val validGrupperingsId = "65432"
    private val validLink = "https://www.nav.no/systemX/"
    private val validSikkerhetsnivaa = 4
    private val validStatusGlobal = "SENDT"
    private val validStatusIntern = "dummyStatusIntern"
    private val validSakstema = "dummySakstema"

    @Test
    fun `skal returnere maskerte data fra toString-metoden`() {
        val statusoppdatering = StatusoppdateringObjectMother.giveMeStatusoppdatering("dummyEventId", "123")
        val statusoppdateringAsString = statusoppdatering.toString()
        statusoppdateringAsString `should contain` "fodselsnummer=***"
        statusoppdateringAsString `should contain` "systembruker=***"
        statusoppdateringAsString `should contain` "link=***"
    }

    @Test
    fun `do not allow too long systembruker`() {
        val tooLongSystembruker = "P".repeat(101)
        invoking {
            Statusoppdatering(
                    systembruker = tooLongSystembruker,
                    eventId = validEventId,
                    eventTidspunkt = eventTidspunkt,
                    fodselsnummer = validFodselsnummer,
                    grupperingsId = validGrupperingsId,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    statusGlobal = validStatusGlobal,
                    statusIntern = validStatusIntern,
                    sakstema = validSakstema)
        } `should throw` FieldValidationException::class `with message containing` "systembruker"
    }

    @Test
    fun `do not allow too long fodselsnummer`() {
        val tooLongFnr = "1".repeat(12)
        invoking {
            Statusoppdatering(
                    systembruker = validSystembruker,
                    eventId = validEventId,
                    eventTidspunkt = eventTidspunkt,
                    fodselsnummer = tooLongFnr,
                    grupperingsId = validGrupperingsId,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    statusGlobal = validStatusGlobal,
                    statusIntern = validStatusIntern,
                    sakstema = validSakstema)
        } `should throw` FieldValidationException::class `with message containing` "fodselsnummer"
    }

    @Test
    fun `do not allow too long eventid`() {
        val tooLongEventId = "E".repeat(51)
        invoking {
            Statusoppdatering(
                    systembruker = validSystembruker,
                    eventId = tooLongEventId,
                    eventTidspunkt = eventTidspunkt,
                    fodselsnummer = validFodselsnummer,
                    grupperingsId = validGrupperingsId,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    statusGlobal = validStatusGlobal,
                    statusIntern = validStatusIntern,
                    sakstema = validSakstema)
        } `should throw` FieldValidationException::class `with message containing` "eventId"
    }

    @Test
    fun `do not allow too long grupperingsId`() {
        val tooLongGrupperingsId = "G".repeat(101)
        invoking {
            Statusoppdatering(
                    systembruker = validSystembruker,
                    eventId = validEventId,
                    eventTidspunkt = eventTidspunkt,
                    fodselsnummer = validFodselsnummer,
                    grupperingsId = tooLongGrupperingsId,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    statusGlobal = validStatusGlobal,
                    statusIntern = validStatusIntern,
                    sakstema = validSakstema)
        } `should throw` FieldValidationException::class `with message containing` "grupperingsId"
    }

    @Test
    fun `do not allow too long link`() {
        val tooLongLink = "http://" + "L".repeat(201)
        invoking {
            Statusoppdatering(
                    systembruker = validSystembruker,
                    eventId = validEventId,
                    eventTidspunkt = eventTidspunkt,
                    fodselsnummer = validFodselsnummer,
                    grupperingsId = validGrupperingsId,
                    link = tooLongLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    statusGlobal = validStatusGlobal,
                    statusIntern = validStatusIntern,
                    sakstema = validSakstema)
        } `should throw` FieldValidationException::class `with message containing` "link"
    }

    @Test
    fun `do not allow invalid link`() {
        val invalidLink = "invalidUrl"
        invoking {
            Statusoppdatering(
                    systembruker = validSystembruker,
                    eventId = validEventId,
                    eventTidspunkt = eventTidspunkt,
                    fodselsnummer = validFodselsnummer,
                    grupperingsId = validGrupperingsId,
                    link = invalidLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    statusGlobal = validStatusGlobal,
                    statusIntern = validStatusIntern,
                    sakstema = validSakstema)
        } `should throw` FieldValidationException::class `with message containing` "link"
    }

    @Test
    fun `should allow empty link`() {
        val statusoppdatering = StatusoppdateringObjectMother.giveMeStatusoppdateringWithLink("")
        statusoppdatering.link `should be equal to` ""
    }

    @Test
    fun `do not allow invalid sikkerhetsnivaa`() {
        val invalidSikkerhetsnivaa = 2
        invoking {
            Statusoppdatering(
                    systembruker = validSystembruker,
                    eventId = validEventId,
                    eventTidspunkt = eventTidspunkt,
                    fodselsnummer = validFodselsnummer,
                    grupperingsId = validGrupperingsId,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = invalidSikkerhetsnivaa,
                    statusGlobal = validStatusGlobal,
                    statusIntern = validStatusIntern,
                    sakstema = validSakstema)
        } `should throw` FieldValidationException::class `with message containing` "Sikkerhetsnivaa"
    }

    @Test
    fun `do not allow invalid statusGlobal`() {
        val invalidStatusGlobal = "invalidStatusGlobal"
        invoking {
            Statusoppdatering(
                    systembruker = validSystembruker,
                    eventId = validEventId,
                    eventTidspunkt = eventTidspunkt,
                    fodselsnummer = validFodselsnummer,
                    grupperingsId = validGrupperingsId,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    statusGlobal = invalidStatusGlobal,
                    statusIntern = validStatusIntern,
                    sakstema = validSakstema)
        } `should throw` FieldValidationException::class `with message containing` "StatusGlobal"
    }

    @Test
    fun `should allow valid statusGlobal`() {
        val validStatusGlobal = "SENDT"
        Statusoppdatering(
                systembruker = validSystembruker,
                eventId = validEventId,
                eventTidspunkt = eventTidspunkt,
                fodselsnummer = validFodselsnummer,
                grupperingsId = validGrupperingsId,
                link = validLink,
                sistOppdatert = sistOppdatert,
                sikkerhetsnivaa = validSikkerhetsnivaa,
                statusGlobal = validStatusGlobal,
                statusIntern = validStatusIntern,
                sakstema = validSakstema)
    }

    @Test
    fun `should allow valid statusGlobal field`() {
        val validStatusGlobal = "MOTTATT"
        Statusoppdatering(
                systembruker = validSystembruker,
                eventId = validEventId,
                eventTidspunkt = eventTidspunkt,
                fodselsnummer = validFodselsnummer,
                grupperingsId = validGrupperingsId,
                link = validLink,
                sistOppdatert = sistOppdatert,
                sikkerhetsnivaa = validSikkerhetsnivaa,
                statusGlobal = validStatusGlobal,
                statusIntern = validStatusIntern,
                sakstema = validSakstema)
    }

    @Test
    fun `do not allow too long statusIntern`() {
        val tooLongStatusIntern = "S".repeat(101)
        invoking {
            Statusoppdatering(
                    systembruker = validSystembruker,
                    eventId = validEventId,
                    eventTidspunkt = eventTidspunkt,
                    fodselsnummer = validFodselsnummer,
                    grupperingsId = validGrupperingsId,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    statusGlobal = validStatusGlobal,
                    statusIntern = tooLongStatusIntern,
                    sakstema = validSakstema)
        } `should throw` FieldValidationException::class `with message containing` "statusIntern"
    }

    @Test
    fun `should allow statusIntern to be null`() {
        val validNullStatusIntern = null
        Statusoppdatering(
                systembruker = validSystembruker,
                eventId = validEventId,
                eventTidspunkt = eventTidspunkt,
                fodselsnummer = validFodselsnummer,
                grupperingsId = validGrupperingsId,
                link = validLink,
                sistOppdatert = sistOppdatert,
                sikkerhetsnivaa = validSikkerhetsnivaa,
                statusGlobal = validStatusGlobal,
                statusIntern = validNullStatusIntern,
                sakstema = validSakstema)
    }

    @Test
    fun `do not allow too long sakstema`() {
        val tooLongSakstema = "S".repeat(51)
        invoking {
            Statusoppdatering(
                    systembruker = validSystembruker,
                    eventId = validEventId,
                    eventTidspunkt = eventTidspunkt,
                    fodselsnummer = validFodselsnummer,
                    grupperingsId = validGrupperingsId,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    statusGlobal = validStatusGlobal,
                    statusIntern = validStatusIntern,
                    sakstema = tooLongSakstema)
        } `should throw` FieldValidationException::class `with message containing` "sakstema"
    }

}