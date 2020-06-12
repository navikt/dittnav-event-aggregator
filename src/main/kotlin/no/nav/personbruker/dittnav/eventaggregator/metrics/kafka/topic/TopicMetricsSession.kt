package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.topic

import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.UniqueKafkaEventIdentifier

class TopicMetricsSession(val eventType: EventType) {

    private val treMillioner = 30000000

    private var duplicatesByProdusent = HashMap<String, Int>(50)
    private var totalNumberOfEventsByProducer = HashMap<String, Int>(50)
    private val uniqueEventsOnTopicByProducer = HashMap<String, Int>(50)
    private val uniqueEventsOnTopic = HashSet<UniqueKafkaEventIdentifier>(treMillioner)

    fun countEvent(event: UniqueKafkaEventIdentifier) {
        val produsent = event.systembruker
        totalNumberOfEventsByProducer[produsent] = totalNumberOfEventsByProducer.getOrDefault(produsent, 0).inc()
        val wasNewUniqueEvent = uniqueEventsOnTopic.add(event)
        if (wasNewUniqueEvent) {
            uniqueEventsOnTopicByProducer[produsent] = uniqueEventsOnTopicByProducer.getOrDefault(produsent, 0).inc()
        } else {
            duplicatesByProdusent[produsent] = duplicatesByProdusent.getOrDefault(produsent, 0).inc()
        }
    }

    fun getNumberOfUniqueEvents(): Int {
        return uniqueEventsOnTopic.size
    }

    fun getNumberOfUniqueEvents(produsent: String): Int {
        return uniqueEventsOnTopicByProducer.getOrDefault(produsent, 0)
    }

    fun getDuplicates(): Int {
        var resultat = 0
        duplicatesByProdusent.forEach { produsent ->
            resultat += produsent.value
        }
        return resultat
    }

    fun getDuplicates(produsent: String): Int {
        return duplicatesByProdusent.getOrDefault(produsent, 0)
    }

    fun getTotalNumber(produsent: String): Int {
        return totalNumberOfEventsByProducer.getOrDefault(produsent, 0)
    }

    fun getTotalNumber(): Int {
        var resultat = 0
        totalNumberOfEventsByProducer.forEach { produsent ->
            resultat += produsent.value
        }
        return resultat
    }

    fun getProducersWithUniqueEvents(): Set<String> {
        return uniqueEventsOnTopicByProducer.keys
    }

    fun getProducersWithDuplicatedEvents(): Set<String> {
        return duplicatesByProdusent.keys
    }

    fun getProducersWithEvents(): Set<String> {
        return totalNumberOfEventsByProducer.keys
    }

    override fun toString(): String {
        return """TopicMetricsSession(
|                   eventType=$eventType, 
|                   unique=${getNumberOfUniqueEvents()}
|                   duplicates=$duplicatesByProdusent, 
|                   total=$totalNumberOfEventsByProducer, 
|                 )""".trimMargin()
    }

}
