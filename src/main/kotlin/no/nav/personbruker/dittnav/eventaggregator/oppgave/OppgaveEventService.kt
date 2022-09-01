package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.brukernotifikasjon.schemas.internal.NokkelIntern
import no.nav.brukernotifikasjon.schemas.internal.OppgaveIntern
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonPersistingService
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.config.EventType.OPPGAVE_INTERN
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.Produsent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory

class OppgaveEventService(
        private val persistingService: BrukernotifikasjonPersistingService<Oppgave>,
        private val metricsProbe: EventMetricsProbe
) : EventBatchProcessorService<NokkelIntern, OppgaveIntern> {

    private val log = LoggerFactory.getLogger(OppgaveEventService::class.java)

    override suspend fun processEvents(events: ConsumerRecords<NokkelIntern, OppgaveIntern>) {
        val successfullyTransformedEvents = mutableListOf<Oppgave>()
        val problematicEvents = mutableListOf<ConsumerRecord<NokkelIntern, OppgaveIntern>>()

        metricsProbe.runWithMetrics(eventType = OPPGAVE_INTERN) {
            events.forEach { event ->
                try {
                    val internalEventValue = OppgaveTransformer.toInternal(event.key(), event.value())
                    successfullyTransformedEvents.add(internalEventValue)
                    countSuccessfulEventForProducer(Produsent(internalEventValue.appnavn, internalEventValue.namespace))
                } catch (e: Exception) {
                    countFailedEventForProducer(Produsent(event.appnavn, event.namespace))
                    problematicEvents.add(event)
                    log.warn("Transformasjon av oppgave-event fra Kafka feilet, fullfører batch-en før pollig stoppes.", e)
                }
            }

            persistingService.writeEventsToCache(successfullyTransformedEvents)
        }

        kastExceptionHvisMislykkedeTransformasjoner(problematicEvents)
    }


    private fun kastExceptionHvisMislykkedeTransformasjoner(problematicEvents: MutableList<ConsumerRecord<NokkelIntern, OppgaveIntern>>) {
        if (problematicEvents.isNotEmpty()) {
            val message = "En eller flere eventer kunne ikke transformeres"
            val exception = UntransformableRecordException(message)
            exception.addContext("antallMislykkedeTransformasjoner", problematicEvents.size)
            throw exception
        }
    }

}
