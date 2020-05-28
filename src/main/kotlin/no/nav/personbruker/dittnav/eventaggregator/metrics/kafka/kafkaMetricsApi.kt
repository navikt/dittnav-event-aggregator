package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get

fun Routing.kafkaEventCountingApi(eventCounterService: EventCounterService) {

    get("/internal/count/all") {
        val numberOfEvents = eventCounterService.countAllEvents()
        call.respondText(text = numberOfEvents.toString(), contentType = ContentType.Text.Plain)
    }

    get("/internal/count/beskjed") {
        val numberOfEvents = eventCounterService.countBeskjeder()
        val responseText = "Antall beskjeder: $numberOfEvents"
        call.respondText(text = responseText, contentType = ContentType.Text.Plain)
    }

    get("/internal/count/innboks") {
        val numberOfEvents = eventCounterService.countInnboksEventer()
        val responseText = "Antall innboks-eventer: $numberOfEvents"
        call.respondText(text = responseText, contentType = ContentType.Text.Plain)
    }

    get("/internal/count/oppgave") {
        val numberOfEvents = eventCounterService.countOppgaver()
        val responseText = "Antall oppgaver: $numberOfEvents"
        call.respondText(text = responseText, contentType = ContentType.Text.Plain)
    }

    get("/internal/count/done") {
        val numberOfEvents = eventCounterService.countDoneEvents()
        val responseText = "Antall done-eventer: $numberOfEvents"
        call.respondText(text = responseText, contentType = ContentType.Text.Plain)
    }

}
