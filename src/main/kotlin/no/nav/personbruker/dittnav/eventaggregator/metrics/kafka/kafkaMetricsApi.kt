package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get

fun Routing.kafkaMetricsApi(eventCounterService: EventCounterService) {

    get("/internal/count/all") {
        val result = eventCounterService.countAllEvents()
        call.respondText(text = result.toString(), contentType = ContentType.Text.Plain)
    }

    get("/internal/count/beskjed") {
        val result = eventCounterService.countBeskjeder()
        val response = "Antall beskjeder: $result"
        call.respondText(text = response, contentType = ContentType.Text.Plain)
    }

    get("/internal/count/innboks") {
        val result = eventCounterService.countInnboksEventer()
        val response = "Antall innboks-eventer: $result"
        call.respondText(text = response, contentType = ContentType.Text.Plain)
    }

    get("/internal/count/oppgave") {
        val result = eventCounterService.countOppgaver()
        val response = "Antall oppgaver: $result"
        call.respondText(text = response, contentType = ContentType.Text.Plain)
    }

    get("/internal/count/done") {
        val result = eventCounterService.countDoneEvents()
        val response = "Antall done-eventer: $result"
        call.respondText(text = response, contentType = ContentType.Text.Plain)
    }

}
