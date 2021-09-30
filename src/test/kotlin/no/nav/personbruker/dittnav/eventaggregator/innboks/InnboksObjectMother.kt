package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.personbruker.dittnav.eventaggregator.oppgave.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveObjectMother
import java.time.LocalDateTime
import java.time.ZoneId

object InnboksObjectMother {

    fun giveMeAktivInnboks(): Innboks {
        return giveMeAktivInnboks("i-1", "123")
    }

    fun giveMeAktivInnboks(eventId: String, fodselsnummer: String): Innboks {
        return giveMeAktivInnboks(eventId = eventId, fodselsnummer = fodselsnummer, "dummySystembruker", "https://nav.no/systemX/")
    }

    fun giveMeAktivInnboks(eventId: String, fodselsnummer: String, systembruker: String): Innboks {
        return giveMeAktivInnboks(eventId = eventId, fodselsnummer = fodselsnummer, systembruker = systembruker, link = "https://nav.no/systemX/")
    }

    fun giveMeAktivInnboksWithLink(link: String): Innboks {
        return giveMeAktivInnboks(eventId = "i-2", fodselsnummer = "123", systembruker = "dummySystembruker", link = link)
    }

    fun giveMeAktivInnboksWithEksternVarslingAndPrefererteKanaler(eksternVarsling: Boolean, prefererteKanaler: List<String>): Innboks {
        val innboks = giveMeAktivInnboks(
            eventId = "O-2",
            fodselsnummer = "123",
            systembruker = "dummySystembruker"
        )
        return innboks.copy(eksternVarsling = eksternVarsling, prefererteKanaler = prefererteKanaler)
    }

    fun giveMeAktivInnboks(eventId: String, fodselsnummer: String, systembruker: String, link: String): Innboks {
        return Innboks(
                systembruker = systembruker,
                eventId = eventId,
                eventTidspunkt = LocalDateTime.now(ZoneId.of("UTC")),
                fodselsnummer = fodselsnummer,
                grupperingsId = "76543",
                tekst = "Dette er innboksnotifikasjon til brukeren",
                link = link,
                sikkerhetsnivaa = 4,
                sistOppdatert = LocalDateTime.now(ZoneId.of("UTC")),
                aktiv = true,
                eksternVarsling = false)
    }

    fun giveMeInaktivInnboks(): Innboks {
        return Innboks(
                systembruker = "dummySystembruker",
                eventId = "76543",
                eventTidspunkt = LocalDateTime.now(ZoneId.of("UTC")),
                fodselsnummer = "12345678901",
                grupperingsId = "76543",
                tekst = "Dette er innboksnotifikasjon til brukeren",
                link = "https://nav.no/systemX/",
                sikkerhetsnivaa = 4,
                sistOppdatert = LocalDateTime.now(ZoneId.of("UTC")),
                aktiv = false,
                eksternVarsling = false)
    }

}
