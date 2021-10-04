package no.nav.personbruker.dittnav.eventaggregator.nokkel

import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern

fun createNokkel(eventId: Int): NokkelIntern = NokkelIntern("dummyUlid",
        eventId.toString(),
        "dummyGrupperingsid",
        "12345",
        "dummyNamespace",
        "dummyAppnavn",
        "dummySystembruker"
)

