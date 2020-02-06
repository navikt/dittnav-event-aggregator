package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.brukernotifikasjon.schemas.Innboks
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory

class InnboksEventService (
        private val innboksRepository: InnboksRepository
) : EventBatchProcessorService<Innboks> {

    private val log = LoggerFactory.getLogger(InnboksEventService::class.java)

    override suspend fun processEvents(events: ConsumerRecords<Nokkel, Innboks>) {

        events.count { event ->
            runCatching {
                InnboksTransformer.toInternal(event.key(), event.value())
            }.onSuccess { innboks ->
                innboksRepository.storeInnboksEventInCache(innboks)
            }.onFailure { error ->
                log.warn("Transformasjon av event [$event] fra Kafka feilet, fullfører batch-en før pollig stoppes.", error)
            }.isFailure
        }.let { failures ->
            if (failures > 0) {
                throw UntransformableRecordException("En eller flere eventer kunne ikke transformeres")
                        .apply{ addContext("antallMislykkedeTransformasjoner", failures) }
            }
        }
    }
}