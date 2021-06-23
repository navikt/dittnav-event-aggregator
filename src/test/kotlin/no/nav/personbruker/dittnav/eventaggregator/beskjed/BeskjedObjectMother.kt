package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.brukernotifikasjon.schemas.builders.domain.PreferertKanal
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.random.Random

object BeskjedObjectMother {

    fun giveMeTwoAktiveBeskjeder(): List<Beskjed> {
        return listOf(
                giveMeAktivBeskjed("b-1", "123"),
                giveMeAktivBeskjed("b-2", "123")
        )
    }

    fun giveMeAktivBeskjed(): Beskjed {
        return giveMeAktivBeskjed("b-1", "123")
    }

    fun giveMeAktivBeskjed(eventId: String, fodselsnummer: String): Beskjed {
        return giveMeAktivBeskjed(eventId = eventId, fodselsnummer = fodselsnummer, systembruker = "dummySystembruker", link = "https://nav.no/systemX/$eventId")
    }

    fun giveMeAktivBeskjed(eventId: String, fodselsnummer: String, systembruker: String): Beskjed {
        return giveMeAktivBeskjed(eventId = eventId, fodselsnummer = fodselsnummer, systembruker =  systembruker, link = "https://nav.no/systemX/$eventId")
    }

    fun giveMeAktivBeskjedWithLink(link: String): Beskjed {
        return giveMeAktivBeskjed(eventId = "B-2", fodselsnummer = "1234", systembruker = "dummySystembruker", link = link)
    }

    fun giveMeAktivBeskjedWithEksternVarslingAndPrefererteKanaler(eksternVarsling: Boolean, prefererteKanaler: List<String>): Beskjed {
        val beskjed = giveMeAktivBeskjed(eventId = "B-3", fodselsnummer = "1234", systembruker = "dummySystembruker", link = "https://nav.no/systemX/")
        return beskjed.copy(eksternVarsling = eksternVarsling, prefererteKanaler = prefererteKanaler)
    }

    private fun giveMeAktivBeskjed(eventId: String, fodselsnummer: String, systembruker: String, link: String): Beskjed {
        return Beskjed(
                uid = Random.nextInt(1, 100).toString(),
                systembruker = systembruker,
                eventTidspunkt = LocalDateTime.now(ZoneId.of("UTC")),
                synligFremTil = LocalDateTime.now(ZoneId.of("UTC")),
                fodselsnummer = fodselsnummer,
                eventId = eventId,
                grupperingsId = "systemA010",
                tekst = "Dette er beskjed til brukeren",
                link = link,
                sistOppdatert = LocalDateTime.now(ZoneId.of("UTC")),
                sikkerhetsnivaa = 4,
                aktiv = true,
                eksternVarsling = false
            )
    }

    fun giveMeInaktivBeskjed(): Beskjed {
        return Beskjed(
                uid = Random.nextInt(1, 100).toString(),
                systembruker = "dummySystembruker",
                eventTidspunkt = LocalDateTime.now(ZoneId.of("UTC")),
                synligFremTil = LocalDateTime.now(ZoneId.of("UTC")),
                fodselsnummer = "123",
                eventId = "b-2",
                grupperingsId = "65432",
                tekst = "Dette er beskjed til brukeren",
                link = "https://nav.no/systemX/",
                sistOppdatert = LocalDateTime.now(ZoneId.of("UTC")),
                sikkerhetsnivaa = 4,
                aktiv = false,
                eksternVarsling = false
            )
    }
}
