package no.nav.personbruker.dittnav.eventaggregator.metrics.db

import no.nav.personbruker.dittnav.eventaggregator.config.EventType

class DBMetricsSession(val eventType: EventType) {

    private val numberOfCachedEventsForProducer = HashMap<String, Int>()

    fun countCachedEventForProducer(producer: String) {
        numberOfCachedEventsForProducer[producer] = numberOfCachedEventsForProducer.getOrDefault(producer, 0).inc()
    }

    fun getUniqueProducers(): List<String> {
        return numberOfCachedEventsForProducer.keys.distinct()
    }

    fun getNumberOfCachedEvents(): Int {
        return numberOfCachedEventsForProducer.size
    }

    fun getNumberOfCachedEventsForProducer(producer: String): Int {
        return numberOfCachedEventsForProducer.getOrDefault(producer, 0)
    }
}
