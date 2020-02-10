package no.nav.personbruker.dittnav.eventaggregator.nokkel

import no.nav.brukernotifikasjon.schemas.Nokkel

fun createNokkel(i: Int): Nokkel = Nokkel("DittNAV", i.toString())
