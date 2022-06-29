package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import no.nav.doknotifikasjon.schemas.DoknotifikasjonStatus
import no.nav.personbruker.dittnav.eventaggregator.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.metrics.DoknotifikasjonStatusMetricsProbe
import org.apache.kafka.clients.consumer.ConsumerRecords

class DoknotifikasjonStatusService(
    private val doknotifikasjonStatusUpdater: DoknotifikasjonStatusUpdater,
    private val metricsProbe: DoknotifikasjonStatusMetricsProbe
): EventBatchProcessorService<String, DoknotifikasjonStatus> {

    override suspend fun processEvents(events: ConsumerRecords<String, DoknotifikasjonStatus>) {
        metricsProbe.runWithMetrics {

            val allStatuses = events.map { it.value() }

            countStatuses(allStatuses)

            val updateResultForBeskjed = doknotifikasjonStatusUpdater.updateStatusForBeskjed(allStatuses)

            val updateResultForOppgave = doknotifikasjonStatusUpdater.updateStatusForOppgave(allStatuses)

            recordUpdateResult(EventType.BESKJED_INTERN, updateResultForBeskjed)
            recordUpdateResult(EventType.OPPGAVE_INTERN, updateResultForOppgave)
        }
    }
}
