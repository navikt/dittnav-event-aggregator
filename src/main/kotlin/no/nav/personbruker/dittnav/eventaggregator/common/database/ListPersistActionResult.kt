package no.nav.personbruker.dittnav.eventaggregator.common.database

import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistFailureReason.CONFLICTING_KEYS
import no.nav.personbruker.dittnav.eventaggregator.common.database.PersistFailureReason.NO_ERROR
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.AggregatorBatchUpdateException

class ListPersistActionResult<T> private constructor(private val resultList: List<RowResult<T>>) {

    fun allEntitiesPersisted() = resultList.all { result ->
        result.status == NO_ERROR
    }

    fun foundConflictingKeys() = resultList.any { result ->
        result.status == CONFLICTING_KEYS
    }

    fun getPersistedEntitites() = resultList.filter { result ->
        result.status == NO_ERROR
    }.map { result ->
        result.entity
    }

    fun getConflictingEntities() = resultList.filter { result ->
        result.status == CONFLICTING_KEYS
    }.map { result ->
        result.entity
    }

    fun getAllEntities() = resultList.map { result ->
        result.entity
    }

    companion object {
        fun <T> mapParamListToResultArray(paramEntities: List<T>, resultArray: IntArray): ListPersistActionResult<T> {
            if (paramEntities.size != resultArray.size) {
                throw AggregatorBatchUpdateException("Lengde på batch update resultat samsvarer ikke med antall parametere.")
            }

            return paramEntities.mapIndexed { index, entity ->
                when (resultArray[index]) {
                    1 -> RowResult(entity, NO_ERROR)
                    0 -> RowResult(entity, CONFLICTING_KEYS)
                    else -> throw AggregatorBatchUpdateException("Udefinert resultat etter batch update.")
                }
            }.let { resultList ->
                ListPersistActionResult(resultList)
            }
        }

        fun <T> mapListOfIndividualResults(paramResultPairs: List<Pair<T, PersistFailureReason>>): ListPersistActionResult<T> {
            return paramResultPairs.map { pair ->
                RowResult(pair.first, pair.second)
            }.let { rowResults ->
                ListPersistActionResult(rowResults)
            }
        }

        fun <T> emptyInstance() : ListPersistActionResult<T> {
            return ListPersistActionResult(emptyList())
        }
    }
}



private data class RowResult<T>( val entity: T, val status: PersistFailureReason)




