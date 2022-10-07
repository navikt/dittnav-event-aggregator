package no.nav.personbruker.dittnav.eventaggregator.common.database

import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistOutcome.NO_INSERT_OR_UPDATE
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistOutcome.SUCCESS
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.AggregatorBatchUpdateException

class ListPersistActionResult<T> private constructor(private val resultList: List<RowResult<T>>) {

    companion object {
        fun <T> mapParamListToResultArray(paramEntities: List<T>, resultArray: IntArray): ListPersistActionResult<T> {
            if (paramEntities.size != resultArray.size) {
                throw AggregatorBatchUpdateException("Lengde på batch update resultat samsvarer ikke med antall parametere.")
            }

            return paramEntities.mapIndexed { index, entity ->
                when (resultArray[index]) {
                    1 -> RowResult(entity, SUCCESS)
                    0 -> RowResult(entity, NO_INSERT_OR_UPDATE)
                    else -> throw AggregatorBatchUpdateException("Udefinert resultat etter batch update.")
                }
            }.let { resultList ->
                ListPersistActionResult(resultList)
            }
        }

        fun <T> mapListOfIndividualResults(paramResultPairs: List<Pair<T, PersistOutcome>>): ListPersistActionResult<T> {
            return paramResultPairs.map { pair ->
                RowResult(pair.first, pair.second)
            }.let { rowResults ->
                ListPersistActionResult(rowResults)
            }
        }
    }
}



private data class RowResult<T>( val entity: T, val status: PersistOutcome)




