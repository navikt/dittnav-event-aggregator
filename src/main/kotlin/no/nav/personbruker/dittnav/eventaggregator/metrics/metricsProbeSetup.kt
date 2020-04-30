package no.nav.personbruker.dittnav.eventaggregator.metrics

import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.metrics.influx.InfluxMetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.metrics.influx.SensuClient

fun buildEventMetricsProbe(environment: Environment, database: Database): EventMetricsProbe {
    val metricsReporter = resolveMetricsReporter(environment)
    val nameResolver = ProducerNameResolver(database)
    val nameScrubber = ProducerNameScrubber(nameResolver)
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
