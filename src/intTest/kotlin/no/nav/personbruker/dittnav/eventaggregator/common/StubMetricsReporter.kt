package no.nav.personbruker.dittnav.eventaggregator.common

import no.nav.personbruker.dittnav.eventaggregator.metrics.MetricsReporter
import org.slf4j.LoggerFactory

class StubMetricsReporter : MetricsReporter {

    val log = LoggerFactory.getLogger(StubMetricsReporter::class.java)

    override suspend fun registerDataPoint(measurement: String, fields: Map<String, Any>, tags: Map<String, String>) {
        log.info("Data point: { measurement: $measurement, fields: $fields, tags: $tags }")
    }
}