package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.metrics

import no.nav.doknotifikasjon.schemas.DoknotifikasjonStatus
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.UpdateStatusResult

class DoknotifikasjonStatusMetricsSession {

    private var allStatusesByProducer = emptyMap<String, Int>()
    private val statusesSuccessfullyUpdatedByType = HashMap<EventType, List<DoknotifikasjonStatus>>()
    private val statusesWithNoChange = HashMap<EventType, List<DoknotifikasjonStatus>>()
    private val statusesWithoutMatch = HashMap<EventType, List<DoknotifikasjonStatus>>()

    private val startTime = System.nanoTime()

    fun countStatuses(dokStatus: List<DoknotifikasjonStatus>) {
        allStatusesByProducer = countStatusesPerProducer(dokStatus)
    }

    fun recordUpdateResult(eventType: EventType, dokStatus: UpdateStatusResult) {
        statusesSuccessfullyUpdatedByType[eventType] = dokStatus.updatedStatuses
        statusesWithNoChange[eventType] = dokStatus.unchangedStatuses
        statusesWithoutMatch[eventType] = dokStatus.unmatchedStatuses
    }

    private fun countStatusesPerProducer(dokStatus: List<DoknotifikasjonStatus>): Map<String, Int> {
        return dokStatus.groupingBy { it.getBestillerId() }.eachCount()
    }

    fun timeElapsedSinceSessionStartNanos(): Long {
        return System.nanoTime() - startTime
    }

    fun getTotalEventsProcessed() = allStatusesByProducer.values.sum()

    fun getTotalEventsByProducer() = allStatusesByProducer

    fun getCountOfStatuesSuccessfullyUpdated(): List<TagPermutationWithCount> {
        return statusesSuccessfullyUpdatedByType.map { (eventType, statuses) ->
            countForEachTagPermutation(eventType.name, statuses)
        }.flatten()
    }

    fun getCountOfStatuesWithNoChange(): List<TagPermutationWithCount> {
        return statusesWithNoChange.map { (eventType, statuses) ->
            countForEachTagPermutation(eventType.name, statuses)
        }.flatten()
    }

    fun getCountOfStatuesWithNoMatch(): List<TagPermutationWithCount> {
        return statusesWithoutMatch.map { (_, statuses) ->
            statuses
        }.reduce { left, right ->
            left.intersect(right).toList()
        }.let {
            countForEachTagPermutation("N/A", it)
        }
    }

    private fun countForEachTagPermutation(eventType: String, statuses: List<DoknotifikasjonStatus>): List<TagPermutationWithCount> {
        return statuses.groupingBy { doknotStatus ->
            doknotStatus.getBestillerId() to doknotStatus.getStatus()
        }.eachCount().map { (bestillerStatus, count) ->
            val (bestiller, status) = bestillerStatus

            TagPermutationWithCount(eventType, bestiller, status, count)
        }
    }
}
