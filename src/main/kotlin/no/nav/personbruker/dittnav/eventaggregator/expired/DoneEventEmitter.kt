package no.nav.personbruker.dittnav.eventaggregator.expired

import no.nav.brukernotifikasjon.schemas.input.DoneInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.KafkaProducerWrapper
import no.nav.personbruker.dittnav.eventaggregator.oppgave.Oppgave
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

class DoneEventEmitter(
    private val kafkaProducerWrapper: KafkaProducerWrapper<DoneInput>,
) {

    fun emittBeskjedDone(beskjeder: List<Beskjed>) {
        beskjeder.forEach {
            val key = createKeyForEvent(it)
            val event = createDoneEvent()

            kafkaProducerWrapper.sendEvent(key, event)
        }
    }

    fun emittOppgaveDone(oppgaver: List<Oppgave>) {
        oppgaver.forEach {
            val key = createKeyForEvent(it)
            val event = createDoneEvent()

            kafkaProducerWrapper.sendEvent(key, event)
        }
    }

    private fun createDoneEvent(
        sistOppdatert: ZonedDateTime = ZonedDateTime.now()
    ): DoneInput {
        val tidspunkt = LocalDateTime.ofInstant(Instant.ofEpochSecond(sistOppdatert.toEpochSecond()), ZoneOffset.UTC)
        return DoneInput(
            tidspunkt.toInstant(ZoneOffset.UTC).toEpochMilli()
        )
    }

    private fun createKeyForEvent(oppgave: Oppgave): NokkelInput {
        return NokkelInput(
            oppgave.eventId,
            oppgave.grupperingsId,
            oppgave.fodselsnummer,
            oppgave.namespace,
            oppgave.appnavn
        )
    }

    private fun createKeyForEvent(beskjed: Beskjed): NokkelInput {
        return NokkelInput(
            beskjed.eventId,
            beskjed.grupperingsId,
            beskjed.fodselsnummer,
            beskjed.namespace,
            beskjed.appnavn
        )
    }
}
