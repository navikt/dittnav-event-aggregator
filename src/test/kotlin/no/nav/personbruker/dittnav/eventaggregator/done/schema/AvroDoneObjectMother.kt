package no.nav.personbruker.dittnav.eventaggregator.done.schema

import no.nav.brukernotifikasjon.schemas.internal.DoneIntern
import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import org.apache.kafka.clients.consumer.ConsumerRecord
import java.time.Instant

object AvroDoneObjectMother {

    fun createDone(): DoneIntern {
        return DoneIntern(
            Instant.now().toEpochMilli(),
            Instant.now().toEpochMilli()
        )
    }

    fun createDoneWithTidspunktAndBehandlet(tidspunkt: Long, behandlet: Long?): DoneIntern {
        return DoneIntern(
            tidspunkt,
            behandlet
        )
    }
}
