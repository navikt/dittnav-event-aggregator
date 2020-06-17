package no.nav.personbruker.dittnav.eventaggregator.metrics.db.count

import no.nav.personbruker.dittnav.eventaggregator.config.EventType

class DbCountingMetricsSession(val eventType: EventType) {

    private val cachedEventsByProducer = HashMap<String, Int>(50)

    fun addEventsByProducer(eventsByProducer: Map<String, Int>) {
        eventsByProducer.forEach { producer ->
            cachedEventsByProducer[producer.key] = cachedEventsByProducer.getOrDefault(producer.key, 0).plus(producer.value)
        }
    }

    fun getProducers(): Set<String> {
        return cachedEventsByProducer.keys
    }

    fun getNumberOfEventsFor(producer: String): Int {
        return cachedEventsByProducer.getOrDefault(producer, 0)
    }

    fun getTotalNumber(): Int {
        var total = 0
        cachedEventsByProducer.forEach { producer ->
            total += producer.value
        }
        return total
    }

    override fun toString(): String {
        return """DbCountingMetricsSession(
|                   eventType=$eventType, 
|                   cachedEventsByProducer=$cachedEventsByProducer
|                 )""".trimMargin()
    }

}
