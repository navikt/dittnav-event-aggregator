package no.nav.personbruker.dittnav.eventaggregator.common

import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import org.apache.kafka.clients.consumer.ConsumerRecords

class SimpleEventCounterService<T>(var eventCounter: Int = 0) : EventBatchProcessorService<T> {

    override suspend fun processEvents(events: ConsumerRecords<NokkelIntern, T>) {
        eventCounter += events.count()
    }

}
