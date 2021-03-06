package no.nav.personbruker.dittnav.eventaggregator.common

import no.nav.brukernotifikasjon.schemas.Nokkel
import org.apache.kafka.clients.consumer.ConsumerRecords

class SimpleEventCounterService<T>(var eventCounter: Int = 0) : EventBatchProcessorService<T> {

    override suspend fun processEvents(events: ConsumerRecords<Nokkel, T>) {
        eventCounter += events.count()
    }

}
