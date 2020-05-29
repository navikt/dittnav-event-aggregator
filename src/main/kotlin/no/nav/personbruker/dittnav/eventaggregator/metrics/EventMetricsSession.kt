package no.nav.personbruker.dittnav.eventaggregator.metrics

import no.nav.personbruker.dittnav.eventaggregator.config.EventType

class EventMetricsSession(val eventType: EventType) {
    private val numberProcessedByProducer = HashMap<String, Int>()
    private val numberFailedByProducer = HashMap<String, Int>()
    private val numberDuplicateKeysByProducer = HashMap<String, Int>()
    private val startTime = System.nanoTime()

    fun countSuccessfulEventForProducer(producer: String) {
        numberProcessedByProducer[producer] = numberProcessedByProducer.getOrDefault(producer, 0).inc()
    }

    fun countFailedEventForProducer(producer: String) {
        numberFailedByProducer[producer] = numberFailedByProducer.getOrDefault(producer, 0).inc()
    }

    fun countDuplicateEventKeysByProducer(producer: String, number: Int = 1) {
        numberDuplicateKeysByProducer[producer] = numberDuplicateKeysByProducer.getOrDefault(producer, 0) + number
    }

    fun getUniqueProducers(): List<String> {
        val producers = ArrayList<String>()
        producers.addAll(numberProcessedByProducer.keys)
        producers.addAll(numberFailedByProducer.keys)
        return producers.distinct()
    }

    fun getEventsSeen(producer: String): Int {
        return getEventsProcessed(producer) + getEventsFailed(producer)
    }

    fun getEventsProcessed(producer: String): Int {
        return numberProcessedByProducer.getOrDefault(producer, 0)
    }

    fun getEventsFailed(producer: String): Int {
        return numberFailedByProducer.getOrDefault(producer, 0)
    }

    fun getDuplicateKeyEvents(producer: String): Int {
        return numberDuplicateKeysByProducer.getOrDefault(producer, 0)
    }

    fun getEventsSeen(): Int {
        return getEventsProcessed() + getEventsFailed()
    }

    fun getEventsProcessed(): Int {
        return numberProcessedByProducer.values.sum()
    }

    fun getEventsFailed(): Int {
        return numberFailedByProducer.values.sum()
    }

    fun getDuplicateKeyEvents(): Int {
        return numberDuplicateKeysByProducer.values.sum()
    }

    fun timeElapsedSinceSessionStartNanos(): Long {
        return System.nanoTime() - startTime
    }
}