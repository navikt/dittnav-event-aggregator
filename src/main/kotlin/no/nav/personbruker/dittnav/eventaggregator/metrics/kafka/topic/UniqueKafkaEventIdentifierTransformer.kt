package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.topic

import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.UniqueKafkaEventIdentifier
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord

object UniqueKafkaEventIdentifierTransformer {

    fun toInternal(external: ConsumerRecord<Nokkel, GenericRecord>): UniqueKafkaEventIdentifier {
        val nokkel = external.key()
        val record = external.value()
        return UniqueKafkaEventIdentifier(
                nokkel.getEventId(),
                nokkel.getSystembruker(),
                record.get("fodselsnummer").toString()
        )
    }

}
