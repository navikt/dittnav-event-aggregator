package no.nav.personbruker.dittnav.eventaggregator.common

import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult

fun <T> emptyPersistResult(): ListPersistActionResult<T> = ListPersistActionResult.mapListOfIndividualResults(emptyList())