package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.topic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.foundRecords
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.resetTheGroupIdsOffsetToZero
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.config.isOtherEnvironmentThanProd
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.temporal.ChronoUnit

class TopicEventCounterService(val topicMetricsProbe: TopicMetricsProbe,
                               val beskjedCountConsumer: KafkaConsumer<Nokkel, GenericRecord>,
                               val innboksCountConsumer: KafkaConsumer<Nokkel, GenericRecord>,
                               val oppgaveCountConsumer: KafkaConsumer<Nokkel, GenericRecord>,
                               val doneCountConsumer: KafkaConsumer<Nokkel, GenericRecord>) {

    private val log = LoggerFactory.getLogger(TopicEventCounterService::class.java)

    suspend fun countEventsAndReportMetrics() = withContext(Dispatchers.IO) {
        val beskjeder = async {
            countAndReportMetricsForBeskjeder()
        }
        val innboks = async {
            countAndReportMetricsForInnboksEventer()
        }
        val oppgave = async {
            countAndReportMetricsForOppgaver()
        }
        val done = async {
            countAndReportMetricsForDoneEvents()
        }

        beskjeder.await()
        innboks.await()
        oppgave.await()
        done.await()
    }

    suspend fun countAndReportMetricsForBeskjeder() {
        try {
            countEventsAndReportMetrics(beskjedCountConsumer, EventType.BESKJED)

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle og rapportere metrics for antall beskjed-eventer", e)
        }
    }

    suspend fun countAndReportMetricsForInnboksEventer() {
        if (isOtherEnvironmentThanProd()) {
            try {
                countEventsAndReportMetrics(innboksCountConsumer, EventType.INNBOKS)

            } catch (e: Exception) {
                log.warn("Klarte ikke å telle og rapportere metrics for antall innboks-eventer", e)
            }
        }
    }

    suspend fun countAndReportMetricsForOppgaver() {
        try {
            countEventsAndReportMetrics(oppgaveCountConsumer, EventType.OPPGAVE)

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle og rapportere metrics for antall oppgave-eventer", e)
        }
    }

    suspend fun countAndReportMetricsForDoneEvents() {
        try {
            countEventsAndReportMetrics(doneCountConsumer, EventType.DONE)

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle og rapportere metrics for antall done-eventer", e)
        }
    }

    private suspend fun countEventsAndReportMetrics(kafkaConsumer: KafkaConsumer<Nokkel, GenericRecord>, eventType: EventType) {
        topicMetricsProbe.runWithMetrics(eventType) {
            var records = kafkaConsumer.poll(Duration.of(5000, ChronoUnit.MILLIS))
            countBatch(records, this)

            while (records.foundRecords()) {
                records = kafkaConsumer.poll(Duration.of(500, ChronoUnit.MILLIS))
                countBatch(records, this)
            }
            kafkaConsumer.resetTheGroupIdsOffsetToZero()
        }
    }

    private fun countBatch(records: ConsumerRecords<Nokkel, GenericRecord>, metricsSession: TopicMetricsSession) {
        records.forEach { record ->
            val event = UniqueKafkaEventIdentifierTransformer.toInternal(record)
            metricsSession.countEvent(event)
        }
    }

}
