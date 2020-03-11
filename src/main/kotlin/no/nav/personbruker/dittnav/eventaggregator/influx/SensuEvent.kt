package no.nav.personbruker.dittnav.eventaggregator.influx

import org.influxdb.dto.Point

data class SensuEvent(
        val dataPoint: Point,
        val name: String = "aggregator-kafka-events"
) {
    fun toJson(): String {
        return """
            {
                "name": "$name",
                "type": "metric",
                "handlers": [ "events_nano" ],
                "status": 0,
                "output": "${dataPoint.lineProtocol()}"
            }
        """.trimIndent()
    }
}