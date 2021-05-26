package no.nav.personbruker.dittnav.eventaggregator.done.schema

import no.nav.brukernotifikasjon.schemas.internal.DoneIntern
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import org.apache.kafka.clients.consumer.ConsumerRecord
import java.time.Instant

object AvroDoneObjectMother {

    private val defaultOffset = 0L
    private val defaultPartition = 0
    private val defaultDummyTopicName = "dummy"
    private val defaultUlid = "123"

    fun createDoneRecord(eventId: String, fodselsnr: String): ConsumerRecord<NokkelIntern, DoneIntern> {
        val key = NokkelIntern("dummySystembruker", eventId, fodselsnr)
        val value = DoneIntern(
                defaultUlid,
                Instant.now().toEpochMilli(),
                "100${eventId}"
        )
        return ConsumerRecord(defaultDummyTopicName, defaultPartition, defaultOffset, key, value)
    }

    fun createDoneRecord(key: NokkelIntern?, value: DoneIntern): ConsumerRecord<NokkelIntern, DoneIntern> {
        return if (key != null) {
            ConsumerRecord(defaultDummyTopicName, defaultPartition, defaultOffset, key, value)

        } else {
            ConsumerRecord<NokkelIntern, DoneIntern>(defaultDummyTopicName, defaultPartition, defaultOffset, null, value)
        }
    }

    fun createDone(eventId: String): DoneIntern {
        return DoneIntern(
                defaultUlid,
                Instant.now().toEpochMilli(),
                "100${eventId}"
        )
    }
}
