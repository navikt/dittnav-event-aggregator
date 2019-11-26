package no.nav.personbruker.dittnav.eventaggregator.beskjed

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.brukernotifikasjon.schemas.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BeskjedEventService(
        private val BeskjedRepository: BeskjedRepository
) : EventBatchProcessorService<Beskjed> {

    private val log: Logger = LoggerFactory.getLogger(BeskjedEventService::class.java)

    override suspend fun processEvents(events: ConsumerRecords<String, Beskjed>) {
        val successfullyTransformedEvents = mutableListOf<no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed>()
        val problematicEvents = mutableListOf<ConsumerRecord<String, Beskjed>>()
        events.forEach { event ->
            try {
                val internalEvent = BeskjedTransformer.toInternal(event.value())
                successfullyTransformedEvents.add(internalEvent)

            } catch (e: Exception) {
                problematicEvents.add(event)
                log.warn("Transformasjon av event fra Kafka feilet, fullfører batch-en før pollig stoppes.", e)
            }
        }

        withContext(Dispatchers.IO) {
            beskjedRepository.writeEventsToCache(successfullyTransformedEvents)
        }

        kastExceptionHvisMislykkedeTransformasjoner(problematicEvents)
    }

    private fun kastExceptionHvisMislykkedeTransformasjoner(problematicEvents: MutableList<ConsumerRecord<String, Beskjed>>) {
        if (problematicEvents.isNotEmpty()) {
            val message = "En eller flere eventer kunne ikke transformeres"
            val exception = UntransformableRecordException(message)
            exception.addContext("antallMislykkedeTransformasjoner", problematicEvents.size)
            throw exception
        }
    }

}
