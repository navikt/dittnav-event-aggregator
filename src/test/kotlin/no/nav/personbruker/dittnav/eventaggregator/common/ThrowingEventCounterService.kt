package no.nav.personbruker.dittnav.eventaggregator.common

import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import org.apache.kafka.clients.consumer.ConsumerRecords

class ThrowingEventCounterService<V>(val exception: Exception? = null, val every: Int = 1) :
    EventBatchProcessorService<NokkelIntern, V> {

    var invocationCounter = 0
    val successfulEventsCounter get() = successfulEvents.size

    val successfulEvents = mutableListOf<V>()

    override suspend fun processEvents(events: ConsumerRecords<NokkelIntern, V>) {
        events.forEach {
            invocationCounter++
            if (exception != null && invocationCounter % every == 0) {
                throw exception
            } else {
                successfulEvents.add(it.value())
            }
        }
    }
}

