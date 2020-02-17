package no.nav.personbruker.dittnav.eventaggregator.common

import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.eventaggregator.config.*
import no.nav.personbruker.dittnav.eventaggregator.config.PrometheusMetricsCollector.registerMessageSeen
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords

interface EventBatchProcessorService<T> {

    suspend fun processEvents(events: ConsumerRecords<Nokkel, T>)

    fun registerMetrics(record: ConsumerRecord<Nokkel, T>) {
        registerMessageSeen(record.topic(), record.key().getSystembruker())
    }

    fun initMetrics(lifetimeMetrics: List<MetricsState>) {
        lifetimeMetrics.forEach { metrics ->
            PrometheusMetricsCollector.setLifetimeMessagesSeen(metrics.topic, metrics.producer, metrics.messagesSeen)
            PrometheusMetricsCollector.setMessageLastSeen(metrics.topic, metrics.producer, metrics.messageLastSeen)
        }
    }
}