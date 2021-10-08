package no.nav.personbruker.dittnav.eventaggregator.metrics

import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class EventMetricsSession(val eventType: EventType) {

    private val log: Logger = LoggerFactory.getLogger(EventMetricsSession::class.java)

    private val numberProcessedByProducer = HashMap<String, Int>()
    private val numberFailedByProducer = HashMap<String, Int>()
    private val numberDuplicateKeysByProducer = HashMap<String, Int>()
    private val startTime = System.nanoTime()
    private var namespace = ""

    fun countSuccessfulEventForProducer(producer: Produsent) {
        namespace = producer.namespace
        numberProcessedByProducer[producer.appnavn] = numberProcessedByProducer.getOrDefault(producer.appnavn, 0).inc()
    }

    fun countFailedEventForProducer(producer: Produsent) {
        namespace = producer.namespace
        numberFailedByProducer[producer.appnavn] = numberFailedByProducer.getOrDefault(producer.appnavn, 0).inc()
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

    fun getNumberDuplicateKeysByProducer(): HashMap<String, Int> {
        return numberDuplicateKeysByProducer
    }

    fun getNamespace(): String {
        return namespace
    }

    fun timeElapsedSinceSessionStartNanos(): Long {
        return System.nanoTime() - startTime
    }

    // Midlertidig logikk for å ikke få alarmer i Slack fra en produsent som sender mange duplikater.
    fun isDuplicatesFromFpinfoHistorikkOnly(): Boolean {
        return if (onlyOneProducerMadeDuplicates()) {
            numberDuplicateKeysByProducer.keys.any { key -> key.contains("fpinfo-historikk") }

        } else {
            false
        }
    }

    private fun onlyOneProducerMadeDuplicates() = numberDuplicateKeysByProducer.size == 1

    fun logAsWarningForAllProducersExceptForFpinfoHistorikk(msg: String) {
        if (isDuplicatesFromFpinfoHistorikkOnly()) {
            log.info(msg)

        } else {
            log.warn(msg)
        }
    }

}
