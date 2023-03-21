package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.metrics

import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusEvent

class DoknotifikasjonStatusMetricsSession {

    private var allStatusesByProducer = emptyMap<String, Int>()
    private val statusesSuccessfullyUpdatedByType = HashMap<EventType, List<DoknotifikasjonStatusEvent>>()
    private val statusesWithoutMatch = HashMap<EventType, List<DoknotifikasjonStatusEvent>>()

    private val startTime = System.nanoTime()

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

    fun getCountOfStatuesWithNoMatch(): List<TagPermutationWithCount> {
        return statusesWithoutMatch.map { (_, statuses) ->
            statuses
        }.reduce { left, right ->
            left.intersect(right).toList()
        }.let {
            countForEachTagPermutation("N/A", it)
        }
    }

    private fun countForEachTagPermutation(eventType: String, statuses: List<DoknotifikasjonStatusEvent>): List<TagPermutationWithCount> {
        return statuses.groupingBy { doknotStatus ->
            doknotStatus.bestillerAppnavn to doknotStatus.status
        }.eachCount().map { (bestillerStatus, count) ->
            val (bestiller, status) = bestillerStatus

            TagPermutationWithCount(eventType, bestiller, status, count)
        }
    }
}
