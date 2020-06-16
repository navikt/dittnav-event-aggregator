package no.nav.personbruker.dittnav.eventaggregator.metrics.db.count

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get

fun Routing.dbCountingApi(topicEventCounterService: DbEventCounterService) {

    get("/internal/metrics/report/db") {
        topicEventCounterService.countEventsAndReportMetrics()
        call.respondText(text = "Metrics for cache-en har blitt rapportert", contentType = ContentType.Text.Plain)
    }

}
