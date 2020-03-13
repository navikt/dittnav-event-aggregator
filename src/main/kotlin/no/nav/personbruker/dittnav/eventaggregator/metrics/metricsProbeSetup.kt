package no.nav.personbruker.dittnav.eventaggregator.metrics

import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.metrics.influx.InfluxMetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.metrics.influx.SensuClient

fun buildEventMetricsProbe(environment: Environment): EventMetricsProbe {
    val metricsReporter = resolveMetricsReporter(environment)
    val nameScrubber = ProducerNameScrubber(environment.producerAliases)
    return EventMetricsProbe(metricsReporter, nameScrubber)
}

private fun resolveMetricsReporter(environment: Environment): MetricsReporter {
    return if (environment.sensuHost == "" || environment.sensuHost == "stub") {
        StubMetricsReporter()
    } else {
        val sensuClient = SensuClient(environment.sensuHost, environment.sensuPort.toInt())
        InfluxMetricsReporter(sensuClient, environment)
    }
}