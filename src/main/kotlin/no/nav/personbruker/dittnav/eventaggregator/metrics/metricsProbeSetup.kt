package no.nav.personbruker.dittnav.eventaggregator.metrics

import no.nav.personbruker.dittnav.common.metrics.MetricsReporter
import no.nav.personbruker.dittnav.common.metrics.StubMetricsReporter
import no.nav.personbruker.dittnav.common.metrics.influx.InfluxMetricsReporter
import no.nav.personbruker.dittnav.common.metrics.influx.SensuConfig
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.DBMetricsProbe

fun buildEventMetricsProbe(environment: Environment, nameScrubber: ProducerNameScrubber): EventMetricsProbe {
    val metricsReporter = resolveMetricsReporter(environment)
    return EventMetricsProbe(metricsReporter, nameScrubber)
}

fun buildDBMetricsProbe(environment: Environment, nameScrubber: ProducerNameScrubber): DBMetricsProbe {
    val metricsReporter = resolveMetricsReporter(environment)
    return DBMetricsProbe(metricsReporter, nameScrubber)
}

private fun resolveMetricsReporter(environment: Environment): MetricsReporter {
    return if (environment.sensuHost == "" || environment.sensuHost == "stub") {
        StubMetricsReporter()
    } else {
        val sensuConfig = createSensuConfig(environment)
        InfluxMetricsReporter(sensuConfig)
    }
}

private fun createSensuConfig(environment: Environment) = SensuConfig(
        hostName = environment.sensuHost,
        hostPort = environment.sensuPort,
        clusterName = environment.clusterName,
        namespace = environment.namespace,
        applicationName = "dittnav-event-aggregator",
        eventsTopLevelName = "aggregator-kafka-events",
        enableEventBatching = true,
)
