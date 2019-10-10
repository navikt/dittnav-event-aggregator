package no.nav.personbruker.dittnav.eventaggregator.service.impl

import no.nav.brukernotifikasjon.schemas.Informasjon
import no.nav.personbruker.dittnav.eventaggregator.exceptions.UntransformableRecordException
import no.nav.personbruker.dittnav.eventaggregator.informasjon.InformasjonRepository
import no.nav.personbruker.dittnav.eventaggregator.service.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.transformer.InformasjonTransformer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class InformasjonEventService(
        private val informasjonRepository: InformasjonRepository,
        private val informasjonTransformer: InformasjonTransformer = InformasjonTransformer()
) : EventBatchProcessorService<Informasjon> {

    private val log: Logger = LoggerFactory.getLogger(InformasjonEventService::class.java)

    override suspend fun processEvents(events: ConsumerRecords<String, Informasjon>) {
        val successfullyTransformedEvents = mutableListOf<no.nav.personbruker.dittnav.eventaggregator.database.entity.Informasjon>()
        val problematicEvents = mutableListOf<ConsumerRecord<String, Informasjon>>()
        events.forEach { event ->
            try {
                val internalEvent = informasjonTransformer.toInternal(event.value())
                successfullyTransformedEvents.add(internalEvent)

            } catch (e: Exception) {
                problematicEvents.add(event)
                log.warn("Transformasjon av event fra Kafka feilet, fullfører batch-en før pollig stoppes.", e)
            }
        }
        informasjonRepository.writeEventsToCache(successfullyTransformedEvents)
        kastExceptionHvisMislykkedeTransformasjoner(problematicEvents)
    }

    private fun kastExceptionHvisMislykkedeTransformasjoner(problematicEvents: MutableList<ConsumerRecord<String, Informasjon>>) {
        if (problematicEvents.isNotEmpty()) {
            val message = "En eller flere eventer kunne ikke transformeres"
            val exception = UntransformableRecordException(message)
            exception.addContext("antallMislykkedeTransformasjoner", problematicEvents.size)
            throw exception
        }
    }

}
