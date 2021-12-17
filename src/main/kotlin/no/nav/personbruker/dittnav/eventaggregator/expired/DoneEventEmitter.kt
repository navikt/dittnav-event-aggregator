package no.nav.personbruker.dittnav.eventaggregator.expired

import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.KafkaProducerWrapper
import no.nav.personbruker.dittnav.eventaggregator.oppgave.Oppgave
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

class DoneEventEmitter(private val kafkaProducerWrapper: KafkaProducerWrapper<Done>) {

    fun emittBeskjedDone(beskjeder: List<Beskjed>) {
        beskjeder.forEach {
            val key = createKeyForEvent(it.eventId, it.systembruker)
            val event = createDoneEvent(it.fodselsnummer, it.grupperingsId)

            kafkaProducerWrapper.sendEvent(key, event)
        }
    }

    fun emittOppgaveDone(oppgaver: List<Oppgave>) {
        oppgaver.forEach {
            val key = createKeyForEvent(it.eventId, it.systembruker)
            val event = createDoneEvent(it.fodselsnummer, it.grupperingsId)

            kafkaProducerWrapper.sendEvent(key, event)
        }
    }

    private fun createDoneEvent(
        fodselsnummer: String,
        grupperingsId: String,
        sistOppdatert: ZonedDateTime = ZonedDateTime.now()
    ): Done {
        val tidspunkt = LocalDateTime.ofInstant(Instant.ofEpochMilli(sistOppdatert.toEpochSecond()), ZoneOffset.UTC)
        return Done(
            tidspunkt.toInstant(ZoneOffset.UTC).toEpochMilli(),
            fodselsnummer,
            grupperingsId
        )
    }

    private fun createKeyForEvent(eventId: String, systembruker: String): Nokkel {
        return Nokkel(
            systembruker,
            eventId
        )
    }
}
