package no.nav.personbruker.dittnav.eventaggregator.common

import no.nav.brukernotifikasjon.schemas.Nokkel
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords

interface EventBatchProcessorService<T> {

    suspend fun processEvents(events: ConsumerRecords<Nokkel, T>)

    val ConsumerRecord<Nokkel, T>.systembruker : String get() = key().getSystembruker()

    val ConsumerRecord<Nokkel, T>.eventId : String get() = key().getEventId()

}
