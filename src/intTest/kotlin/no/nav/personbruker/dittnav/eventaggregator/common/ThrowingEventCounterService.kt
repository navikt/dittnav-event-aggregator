package no.nav.personbruker.dittnav.eventaggregator.common

import no.nav.brukernotifikasjon.schemas.Nokkel
import org.apache.kafka.clients.consumer.ConsumerRecords

class ThrowingEventCounterService<T>(val exception: Exception? = null, val every: Int = 1) : EventBatchProcessorService<T> {

    var invocationCounter = 0
    val successfulEventsCounter get() = successfulEvents.size

    val successfulEvents = mutableListOf<T>()

    override suspend fun processEvents(events: ConsumerRecords<Nokkel, T>) {
        invocationCounter += events.count()

        if (exception != null && invocationCounter % every == 0) {
            throw exception
        } else {
            events.map {
                it.value()
            }.let {
                successfulEvents.addAll(it)
            }
        }
    }
}

