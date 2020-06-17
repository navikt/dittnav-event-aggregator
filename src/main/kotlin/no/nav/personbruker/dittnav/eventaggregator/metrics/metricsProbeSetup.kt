package no.nav.personbruker.dittnav.eventaggregator.metrics

import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.config.Environment
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.DBMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.count.DbCountingMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.influx.InfluxMetricsReporter
import no.nav.personbruker.dittnav.eventaggregator.metrics.influx.SensuClient
import no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.topic.TopicMetricsProbe

fun buildEventMetricsProbe(environment: Environment, database: Database): EventMetricsProbe {
    val metricsReporter = resolveMetricsReporter(environment)
    val nameResolver = ProducerNameResolver(database)
    val nameScrubber = ProducerNameScrubber(nameResolver)
    return EventMetricsProbe(metricsReporter, nameScrubber)
}

fun buildDBMetricsProbe(environment: Environment, database: Database): DBMetricsProbe {
    val metricsReporter = resolveMetricsReporter(environment)
    val nameResolver = ProducerNameResolver(database)
    val nameScrubber = ProducerNameScrubber(nameResolver)
    return DBMetricsProbe(metricsReporter, nameScrubber)
}

fun buildTopicMetricsProbe(environment: Environment, database: Database): TopicMetricsProbe {
    val metricsReporter = resolveMetricsReporter(environment)
    val nameResolver = ProducerNameResolver(database)
    val nameScrubber = ProducerNameScrubber(nameResolver)
    return TopicMetricsProbe(metricsReporter, nameScrubber)
}

fun buildDbEventCountingMetricsProbe(environment: Environment, database: Database): DbCountingMetricsProbe {
    val metricsReporter = resolveMetricsReporter(environment)
    val nameResolver = ProducerNameResolver(database)
    val nameScrubber = ProducerNameScrubber(nameResolver)
    return DbCountingMetricsProbe(metricsReporter, nameScrubber)
}

private fun resolveMetricsReporter(environment: Environment): MetricsReporter {
    return if (environment.sensuHost == "" || environment.sensuHost == "stub") {
        StubMetricsReporter()
    } else {
        val sensuClient = SensuClient(environment.sensuHost, environment.sensuPort.toInt())
        InfluxMetricsReporter(sensuClient, environment)
    }
}
