package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get

fun Routing.kafkaMetricsApi(eventCounterService: EventCounterService) {

    get("/internal/countEvents") {
        val result = eventCounterService.countEvents()
        call.respondText(text = result.toString(), contentType = ContentType.Text.Plain)
    }

}
