package no.nav.personbruker.dittnav.eventaggregator.common

import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords

interface EventBatchProcessorService<T> {

    suspend fun processEvents(events: ConsumerRecords<NokkelIntern, T>)

    val ConsumerRecord<NokkelIntern, T>.namespace: String get() = key().getNamespace()

    val ConsumerRecord<NokkelIntern, T>.appnavn: String get() = key().getAppnavn()

    val ConsumerRecord<NokkelIntern, T>.systembruker: String get() = key().getSystembruker()

    val ConsumerRecord<NokkelIntern, T>.eventId: String get() = key().getEventId()

}
