package no.nav.personbruker.dittnav.eventaggregator.done.schema

import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Nokkel
import org.apache.kafka.clients.consumer.ConsumerRecord
import java.time.Instant

object AvroDoneObjectMother {

    private val defaultOffset = 0L
    private val defaultPartition = 0
    private val defaultDummtTopicName = "dummy"

    fun createDoneRecord(eventId: String, fodselsnr: String): ConsumerRecord<Nokkel, Done> {
        val key = Nokkel("dummyProducer", eventId)
        val value = Done(
                Instant.now().toEpochMilli(),
                fodselsnr,
                "100${eventId}"
        )
        return ConsumerRecord(defaultDummtTopicName, defaultPartition, defaultOffset, key, value)
    }

    fun createDoneRecord(key: Nokkel?, value: Done): ConsumerRecord<Nokkel, Done> {
        return if (key != null) {
            ConsumerRecord(defaultDummtTopicName, defaultPartition, defaultOffset, key, value)

        } else {
            ConsumerRecord<Nokkel, Done>(defaultDummtTopicName, defaultPartition, defaultOffset, null, value)
        }
    }

    fun createDone(eventId: String): Done {
        return Done(
                Instant.now().toEpochMilli(),
                "12345",
                "100${eventId}"
        )
    }

    fun createDone(eventId: String, fodselsnummer: String): Done {
        return Done(
                Instant.now().toEpochMilli(),
                fodselsnummer,
                "100${eventId}"
        )
    }
}
