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
        val key = NokkelIntern(defaultUlid,
                eventId,
                "dummyGrupperingsid",
                fodselsnr,
                "dummyNamespace",
                "dummyAppnavn",
                "dummySystembruker")

        val value = DoneIntern(
                Instant.now().toEpochMilli()
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

    fun createDone(): DoneIntern {
        return DoneIntern(
                Instant.now().toEpochMilli()
        )
    }
}
