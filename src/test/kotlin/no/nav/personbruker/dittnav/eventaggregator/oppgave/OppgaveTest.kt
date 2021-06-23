package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.brukernotifikasjon.schemas.builders.domain.PreferertKanal
import no.nav.brukernotifikasjon.schemas.builders.exception.FieldValidationException
import no.nav.personbruker.dittnav.eventaggregator.common.`with message containing`
import org.amshove.kluent.`should contain`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.invoking
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId

class OppgaveTest {

    private val validSystembruker = "dummySystembruker"
    private val validFodselsnummer = "123"
    private val eventTidspunkt = LocalDateTime.now(ZoneId.of("UTC"))
    private val sistOppdatert = LocalDateTime.now(ZoneId.of("UTC"))
    private val validEventId = "b-2"
    private val validGrupperingsId = "65432"
    private val validTekst = "Dette er en oppgave til brukeren"
    private val validLink = "https://www.nav.no/systemX/"
    private val validSikkerhetsnivaa = 4

    @Test
    fun `skal returnere maskerte data fra toString-metoden`() {
        val oppgave = OppgaveObjectMother.giveMeAktivOppgave("dummyEventId", "123")
        val oppgaveAsString = oppgave.toString()
        oppgaveAsString `should contain` "fodselsnummer=***"
        oppgaveAsString `should contain` "tekst=***"
        oppgaveAsString `should contain` "link=***"
    }

    @Test
    fun `do not allow too long systembruker`() {
        val tooLongSystembruker = "P".repeat(101)
        invoking {
            Oppgave(
                    systembruker = tooLongSystembruker,
                    eventTidspunkt = eventTidspunkt,
                    fodselsnummer = validFodselsnummer,
                    eventId = validEventId,
                    grupperingsId = validGrupperingsId,
                    tekst = validTekst,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    aktiv = true,
                    eksternVarsling = false
            )
        } `should throw` FieldValidationException::class `with message containing` "systembruker"
    }

    @Test
    fun `do not allow too long fodselsnummer`() {
        val tooLongFnr = "1".repeat(12)
        invoking {
            Oppgave(
                    systembruker = validSystembruker,
                    eventTidspunkt = eventTidspunkt,
                    fodselsnummer = tooLongFnr,
                    eventId = validEventId,
                    grupperingsId = validGrupperingsId,
                    tekst = validTekst,
                    link = validLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    aktiv = true,
                    eksternVarsling = false
            )
        } `should throw` FieldValidationException::class `with message containing` "fodselsnummer"
    }

    @Test
    fun `do not allow too long eventid`() {
        val tooLongEventId = "E".repeat(51)
        invoking {
            Oppgave(
                    systembruker = validSystembruker,
                    eventTidspunkt = eventTidspunkt,
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
            Oppgave(
                    systembruker = validSystembruker,
                    eventTidspunkt = eventTidspunkt,
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
            Oppgave(
                    systembruker = validSystembruker,
                    eventTidspunkt = eventTidspunkt,
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
        val tooLongLink = "http://" + "L".repeat(201)
        invoking {
            Oppgave(

                    systembruker = validSystembruker,
                    eventTidspunkt = eventTidspunkt,
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
    fun `do not allow invalid link`() {
        val invalidLink = "invalidUrl"
        invoking {
            Oppgave(

                    systembruker = validSystembruker,
                    eventTidspunkt = eventTidspunkt,
                    fodselsnummer = validFodselsnummer,
                    eventId = validEventId,
                    grupperingsId = validGrupperingsId,
                    tekst = validTekst,
                    link = invalidLink,
                    sistOppdatert = sistOppdatert,
                    sikkerhetsnivaa = validSikkerhetsnivaa,
                    aktiv = true,
                    eksternVarsling = false)
        } `should throw` FieldValidationException::class `with message containing` "link"
    }

    @Test
    fun `do not allow empty link`() {
        val emptyLink = ""
        invoking {
            Oppgave(
                    systembruker = validSystembruker,
                    eventTidspunkt = eventTidspunkt,
                    fodselsnummer = validFodselsnummer,
                    eventId = validEventId,
                    grupperingsId = validGrupperingsId,
                    tekst = validTekst,
                    link = emptyLink,
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
            Oppgave(
                    systembruker = validSystembruker,
                    eventTidspunkt = eventTidspunkt,
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
    fun `do not allow unknown preferert kanal`() {
        val unknownPreferertKanal = "Unknown"
        invoking {
            Oppgave(
                systembruker = validSystembruker,
                eventTidspunkt = eventTidspunkt,
                fodselsnummer = validFodselsnummer,
                eventId = validEventId,
                grupperingsId = validGrupperingsId,
                tekst = validTekst,
                link = validLink,
                sistOppdatert = sistOppdatert,
                sikkerhetsnivaa = validSikkerhetsnivaa,
                aktiv = true,
                eksternVarsling = true,
                prefererteKanaler = listOf(unknownPreferertKanal)
            )
        } `should throw` FieldValidationException::class `with message containing` "prefererteKanaler"
    }

    @Test
    fun `do not allow prefererteKanaler if eksternVarsling is false`() {
        invoking {
            Oppgave(
                systembruker = validSystembruker,
                eventTidspunkt = eventTidspunkt,
                fodselsnummer = validFodselsnummer,
                eventId = validEventId,
                grupperingsId = validGrupperingsId,
                tekst = validTekst,
                link = validLink,
                sistOppdatert = sistOppdatert,
                sikkerhetsnivaa = validSikkerhetsnivaa,
                aktiv = true,
                eksternVarsling = false,
                prefererteKanaler = listOf(PreferertKanal.SMS.toString())
            )
        } `should throw` FieldValidationException::class `with message containing` "prefererteKanaler"
    }
}
