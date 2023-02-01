package no.nav.personbruker.dittnav.eventaggregator.metrics

import no.nav.personbruker.dittnav.common.metrics.MetricsReporter
import no.nav.personbruker.dittnav.common.metrics.StubMetricsReporter
import no.nav.personbruker.dittnav.common.metrics.influxdb.InfluxConfig
import no.nav.personbruker.dittnav.common.metrics.influxdb.InfluxMetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.archive.ArchiveMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.expired.ExpiredMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.DBMetricsProbe
import java.util.concurrent.TimeUnit

fun buildDBMetricsProbe(environment: Environment): DBMetricsProbe {
    val metricsReporter = resolveMetricsReporter(environment)
    return DBMetricsProbe(metricsReporter)
}

fun buildArchivingMetricsProbe(environment: Environment): ArchiveMetricsProbe {
    val metricsReporter = resolveMetricsReporter(environment)
    return ArchiveMetricsProbe(metricsReporter)
}

fun buildExpiredMetricsProbe(environment: Environment): ExpiredMetricsProbe {
    return ExpiredMetricsProbe(resolveMetricsReporter(environment))
}

fun buildRapidMetricsProbe(environment: Environment) = RapidMetricsProbe(resolveMetricsReporter(environment))

private fun resolveMetricsReporter(environment: Environment): MetricsReporter {
    return if (environment.influxdbHost == "" || environment.influxdbHost == "stub") {
        StubMetricsReporter()
    } else {
        val sensuConfig = createInfluxConfig(environment)
        InfluxMetricsReporter(sensuConfig)
    }
}

private fun createInfluxConfig(environment: Environment) = InfluxConfig(
        applicationName = "dittnav-event-aggregator",
        hostName = environment.influxdbHost,
        hostPort = environment.influxdbPort,
        databaseName = environment.influxdbName,
        retentionPolicyName = environment.influxdbRetentionPolicy,
        clusterName = environment.clusterName,
        namespace = environment.namespace,
        userName = environment.influxdbUser,
        password = environment.influxdbPassword,
        timePrecision = TimeUnit.NANOSECONDS
)
