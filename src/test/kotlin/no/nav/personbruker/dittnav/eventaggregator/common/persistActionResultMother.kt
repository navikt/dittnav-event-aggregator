package no.nav.personbruker.dittnav.eventaggregator.common

import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistOutcome

fun <T> successfulEvents(events: List<T>): ListPersistActionResult<T> {
    return events.map{ event ->
        event to PersistOutcome.SUCCESS
    }.let { entryList ->
        ListPersistActionResult.mapListOfIndividualResults(entryList)
    }
}

fun <T> emptyPersistResult(): ListPersistActionResult<T> = ListPersistActionResult.mapListOfIndividualResults(emptyList())

fun <T> createPersistActionResult(successful: List<T>, unchanged: List<T>): ListPersistActionResult<T> {
    val successfulPart = successful.map { entry ->
        entry to PersistOutcome.SUCCESS
    }

    val unchangedPart = unchanged.map { entry ->
        entry to PersistOutcome.NO_INSERT_OR_UPDATE
    }

    return (successfulPart + unchangedPart).let {
        ListPersistActionResult.mapListOfIndividualResults(it)
    }
}
