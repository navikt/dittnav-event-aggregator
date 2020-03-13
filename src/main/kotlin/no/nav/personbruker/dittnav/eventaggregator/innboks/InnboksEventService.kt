package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.brukernotifikasjon.schemas.Innboks
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.NokkelNullException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.serializer.getNonNullKey
import no.nav.personbruker.dittnav.eventaggregator.config.EventType.INNBOKS
import no.nav.personbruker.dittnav.eventaggregator.metrics.EventMetricsProbe
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory

class InnboksEventService (
        private val innboksRepository: InnboksRepository,
        private val metricsProbe: EventMetricsProbe
) : EventBatchProcessorService<Innboks> {

    private val log = LoggerFactory.getLogger(InnboksEventService::class.java)

    override suspend fun processEvents(events: ConsumerRecords<Nokkel, Innboks>) {
        val problematicEvents = mutableListOf<ConsumerRecord<Nokkel, Innboks>>()
        events.forEach { event ->
            try {
                metricsProbe.reportEventSeen(INNBOKS, event.systembruker)
                val internalEvent = InnboksTransformer.toInternal(event.getNonNullKey(), event.value())
                innboksRepository.storeInnboksEventInCache(internalEvent)
                metricsProbe.reportEventProcessed(INNBOKS, event.systembruker)
            } catch (e: NokkelNullException) {
                metricsProbe.reportEventFailed(INNBOKS, event.systembruker)
                log.warn("Eventet manglet nøkkel. Topic: ${event.topic()}, Partition: ${event.partition()}, Offset: ${event.offset()}", e)
            } catch (e: Exception) {
                metricsProbe.reportEventFailed(INNBOKS, event.systembruker)
                problematicEvents.add(event)
                log.warn("Transformasjon av innboks-event fra Kafka feilet, fullfører batch-en før pollig stoppes.", e)
            }
        }
        kastExceptionHvisMislykkedeTransformasjoner(problematicEvents)
    }

    private fun kastExceptionHvisMislykkedeTransformasjoner(problematicEvents: MutableList<ConsumerRecord<Nokkel, Innboks>>) {
        if (problematicEvents.isNotEmpty()) {
            val message = "En eller flere eventer kunne ikke transformeres"
            val exception = UntransformableRecordException(message)
            exception.addContext("antallMislykkedeTransformasjoner", problematicEvents.size)
            throw exception
        }
    }
}