package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.topic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.common.kafka.resetTheGroupIdsOffsetToZero
import no.nav.personbruker.dittnav.eventaggregator.config.*
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.temporal.ChronoUnit

class TopicEventCounterService(val environment: Environment, val topicMetricsProbe: TopicMetricsProbe) {

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

    private suspend fun countAndReportMetricsForBeskjeder() {
        val beskjedConsumer = createCountConsumer<GenericRecord>(EventType.BESKJED, Kafka.beskjedTopicName, environment)
        try {
            countEventsAndReportMetrics(beskjedConsumer, EventType.BESKJED)

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle og rapportere metrics for antall beskjed-eventer", e)
        }
    }

    private suspend fun countAndReportMetricsForInnboksEventer() {
        if (isOtherEnvironmentThanProd()) {
            val innboksConsumer = createCountConsumer<GenericRecord>(EventType.INNBOKS, Kafka.innboksTopicName, environment)
            try {
                countEventsAndReportMetrics(innboksConsumer, EventType.INNBOKS)

            } catch (e: Exception) {
                log.warn("Klarte ikke å telle og rapportere metrics for antall innboks-eventer", e)
            }
        }
    }

    private suspend fun countAndReportMetricsForOppgaver() {
        val oppgaveConsumer = createCountConsumer<GenericRecord>(EventType.OPPGAVE, Kafka.oppgaveTopicName, environment)
        try {
            countEventsAndReportMetrics(oppgaveConsumer, EventType.OPPGAVE)

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle og rapportere metrics for antall oppgave-eventer", e)
        }
    }

    private suspend fun countAndReportMetricsForDoneEvents() {
        val doneConsumer = createCountConsumer<GenericRecord>(EventType.DONE, Kafka.doneTopicName, environment)
        try {
            countEventsAndReportMetrics(doneConsumer, EventType.DONE)

        } catch (e: Exception) {
            log.warn("Klarte ikke å telle og rapportere metrics for antall done-eventer", e)
        }
    }

    fun <T> createCountConsumer(eventType: EventType,
                                topic: String,
                                environment: Environment,
                                enableSecurity: Boolean = ConfigUtil.isCurrentlyRunningOnNais()): KafkaConsumer<Nokkel, T> {

        val envWithMetricsGroupId = environment.copy(
                counterGroupId = "${environment.counterGroupId}_metrics"
        )
        val kafkaProps = Kafka.counterConsumerProps(envWithMetricsGroupId, eventType, enableSecurity)
        val consumer = KafkaConsumer<Nokkel, T>(kafkaProps)
        consumer.subscribe(listOf(topic))
        return consumer
    }

    suspend fun countEventsAndReportMetrics(kafkaConsumer: KafkaConsumer<Nokkel, GenericRecord>, eventType: EventType) {
        topicMetricsProbe.runWithMetrics(eventType) {
            kafkaConsumer.use { consumer ->
                var records = consumer.poll(Duration.of(5000, ChronoUnit.MILLIS))
                countBatch(records, this)

                while (records.foundRecords()) {
                    records = kafkaConsumer.poll(Duration.of(500, ChronoUnit.MILLIS))
                    countBatch(records, this)
                }
                consumer.resetTheGroupIdsOffsetToZero()
            }
        }
    }

    private fun countBatch(records: ConsumerRecords<Nokkel, GenericRecord>, metricsSession: TopicMetricsSession) {
        records.forEach { record ->
            val event = UniqueKafkaEventIdentifierTransformer.toInternal(record)
            metricsSession.countEvent(event)
        }
    }

}

fun <T> ConsumerRecords<Nokkel, T>.foundRecords(): Boolean {
    return !isEmpty
}
