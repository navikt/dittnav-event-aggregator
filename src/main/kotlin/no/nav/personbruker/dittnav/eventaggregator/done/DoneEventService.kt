package no.nav.personbruker.dittnav.eventaggregator.done

import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.beskjed.setBeskjedAktivFlag
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.Brukernotifikasjon
import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.getAllBrukernotifikasjonFromView
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.NokkelNullException
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.serializer.getNonNullKey
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.innboks.setInnboksAktivFlag
import no.nav.personbruker.dittnav.eventaggregator.oppgave.setOppgaveAktivFlag
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DoneEventService(
        val database: Database
) : EventBatchProcessorService<Done> {

    private val log: Logger = LoggerFactory.getLogger(DoneEventService::class.java)

    override suspend fun processEvents(events: ConsumerRecords<Nokkel, Done>) {
        events.forEach { event ->
            try {
                registerMetrics(event)
                processDoneEvent(event)
            } catch (e: NokkelNullException) {
                log.warn("Eventet manglet nøkkel. Topic: ${event.topic()}, Partition: ${event.partition()}, Offset: ${event.offset()}", e)
            } catch (e: Exception) {
                log.warn("Transformasjon av done-event fra Kafka feilet.", e)
            }
        }
    }

    private suspend fun processDoneEvent(event: ConsumerRecord<Nokkel, Done>) {
        val doneEntity = DoneTransformer.toInternal(event.getNonNullKey(), event.value())
        val brukernotifikasjoner = database.dbQuery { getAllBrukernotifikasjonFromView() }
        val foundEvent: Brukernotifikasjon? = brukernotifikasjoner.find { isEventInCache(it, doneEntity) }
        if (foundEvent != null) {
            log.info("Fant matchende event for Done-event med eventId: ${foundEvent.eventId}, produsent: ${foundEvent.produsent}, type: ${foundEvent.type}")
            flagEventAsInactive(foundEvent, doneEntity)
        } else {
            database.dbQuery { createDoneEvent(doneEntity) }
            log.info("Fant ikke matchende event for done-event med eventId ${doneEntity.eventId}, produsent: ${doneEntity.produsent}, eventTidspunkt: ${doneEntity.eventTidspunkt}. Skrev done-event til cache")
        }
    }

    private fun isEventInCache(brukernotifikasjon: Brukernotifikasjon, done: no.nav.personbruker.dittnav.eventaggregator.done.Done): Boolean {
        return (brukernotifikasjon.eventId == done.eventId &&
                brukernotifikasjon.produsent == done.produsent &&
                brukernotifikasjon.fodselsnummer == done.fodselsnummer)
    }

    suspend fun flagEventAsInactive(event: Brukernotifikasjon, done: no.nav.personbruker.dittnav.eventaggregator.done.Done) {
        when (event.type) {
            EventType.OPPGAVE -> {
                database.dbQuery { setOppgaveAktivFlag(done.eventId, done.produsent, done.fodselsnummer, false) }
                log.info("Satte Oppgave-event med eventId ${event.eventId} inaktivt")
            }
            EventType.BESKJED -> {
                database.dbQuery { setBeskjedAktivFlag(done.eventId, done.produsent, done.fodselsnummer, false) }
                log.info("Satte Beskjed-event med eventId ${event.eventId} inaktivt")
            }
            EventType.INNBOKS -> {
                database.dbQuery { setInnboksAktivFlag(done.eventId, done.produsent, done.fodselsnummer, false) }
                log.info("Satte Innboks-event med eventId ${event.eventId} inaktivt")
            }
        }
    }
}
