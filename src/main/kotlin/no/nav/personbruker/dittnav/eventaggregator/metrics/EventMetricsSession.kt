package no.nav.personbruker.dittnav.eventaggregator.metrics

import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class EventMetricsSession(val eventType: EventType) {

    private val log: Logger = LoggerFactory.getLogger(EventMetricsSession::class.java)

    private val numberProcessedByProducer = HashMap<Produsent, Int>()
    private val numberFailedByProducer = HashMap<Produsent, Int>()
    private val numberDuplicateKeysByProducer = HashMap<Produsent, Int>()
    private val startTime = System.nanoTime()

    fun countSuccessfulEventForProducer(producer: Produsent) {
        numberProcessedByProducer[producer] = numberProcessedByProducer.getOrDefault(producer, 0).inc()
    }

    fun countFailedEventForProducer(producer: Produsent) {
        numberFailedByProducer[producer] = numberFailedByProducer.getOrDefault(producer, 0).inc()
    }

    fun countDuplicateEventKeysByProducer(producer: Produsent, number: Int = 1) {
        numberDuplicateKeysByProducer[producer] = numberDuplicateKeysByProducer.getOrDefault(producer, 0) + number
    }

    fun getUniqueProducers(): List<Produsent> {
        val producers = ArrayList<Produsent>()
        producers.addAll(numberProcessedByProducer.keys)
        producers.addAll(numberFailedByProducer.keys)
        return producers.distinct()
    }

    fun getEventsSeen(producer: Produsent): Int {
        return getEventsProcessed(producer) + getEventsFailed(producer)
    }

    fun getEventsProcessed(producer: Produsent): Int {
        return numberProcessedByProducer.getOrDefault(producer, 0)
    }

    fun getEventsFailed(producer: Produsent): Int {
        return numberFailedByProducer.getOrDefault(producer, 0)
    }

    fun getDuplicateKeyEvents(producer: Produsent): Int {
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

    fun getNumberDuplicateKeysByProducer(): HashMap<Produsent, Int> {
        return numberDuplicateKeysByProducer
    }

    fun timeElapsedSinceSessionStartNanos(): Long {
        return System.nanoTime() - startTime
    }

    // Midlertidig logikk for å ikke få alarmer i Slack fra en produsent som sender mange duplikater.
    fun isDuplicatesFromFpinfoHistorikkOnly(): Boolean {
        return if (onlyOneProducerMadeDuplicates()) {
            numberDuplicateKeysByProducer.keys.any { key -> key.appnavn.contains("fpinfo-historikk") }

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
