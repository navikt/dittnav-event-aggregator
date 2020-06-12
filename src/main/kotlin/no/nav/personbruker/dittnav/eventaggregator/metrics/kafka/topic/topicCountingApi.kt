package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka.topic

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get

fun Routing.topicCountingApi(topicEventCounterService: TopicEventCounterService) {

    get("/internal/metrics/report/topic") {
        topicEventCounterService.countEventsAndReportMetrics()
        call.respondText(text = "Metrics har blitt rapporter", contentType = ContentType.Text.Plain)
    }

}