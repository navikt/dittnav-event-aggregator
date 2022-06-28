package no.nav.personbruker.dittnav.eventaggregator.common

import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import org.apache.kafka.clients.consumer.ConsumerRecords

class SimpleEventCounterService<K,  V>(var eventCounter: Int = 0) : EventBatchProcessorService<K, V> {

    override suspend fun processEvents(events: ConsumerRecords<K, V>) {
        eventCounter += events.count()
    }

}
