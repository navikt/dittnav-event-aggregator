package no.nav.personbruker.dittnav.eventaggregator.metrics.db.count

import no.nav.personbruker.dittnav.eventaggregator.config.EventType

class DbCountingMetricsSession(val eventType: EventType) {

    private val cachedEventsByProducer = HashMap<String, Int>(50)

    fun addEventsByProducent(eventerPerProducer: Map<String, Int>) {
        eventerPerProducer.forEach { produsent ->
            cachedEventsByProducer[produsent.key] = cachedEventsByProducer.getOrDefault(produsent.key, 0).plus(produsent.value)
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

}
