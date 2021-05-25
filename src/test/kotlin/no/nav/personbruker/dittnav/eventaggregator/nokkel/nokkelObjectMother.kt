package no.nav.personbruker.dittnav.eventaggregator.nokkel

import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern

fun createNokkel(eventId: Int): NokkelIntern = NokkelIntern("dummySystembruker", eventId.toString(), "12345")
