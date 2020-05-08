package no.nav.personbruker.dittnav.eventaggregator.nokkel

import no.nav.brukernotifikasjon.schemas.Nokkel

fun createNokkel(eventId: Int): Nokkel = Nokkel("dummySystembruker", eventId.toString())
