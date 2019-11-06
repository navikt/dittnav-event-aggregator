package no.nav.personbruker.dittnav.eventaggregator.melding

import no.nav.brukernotifikasjon.schemas.Melding
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UntransformableRecordException
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory

class MeldingEventService (
        private val meldingRepository: MeldingRepository
) : EventBatchProcessorService<Melding> {

    private val log = LoggerFactory.getLogger(MeldingEventService::class.java)

    override suspend fun processEvents(events: ConsumerRecords<String, Melding>) {

        events.map {
            runCatching {
                MeldingTransformer.toInternal(it.value())
            }
        }.count {
            it.onSuccess { melding ->
                meldingRepository.storeMeldingEventInCache(melding)
            }.onFailure { error ->
                log.warn("Transformasjon av event fra Kafka feilet, fullfører batch-en før pollig stoppes.", error)
            }.isFailure
        }.let { failures ->
            if (failures > 0) {
                throw UntransformableRecordException("En eller flere eventer kunne ikke transformeres")
                        .apply{ addContext("antallMislykkedeTransformasjoner", failures) }
            }
        }
    }
}