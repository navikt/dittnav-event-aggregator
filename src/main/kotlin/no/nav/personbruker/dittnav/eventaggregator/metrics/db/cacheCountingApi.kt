package no.nav.personbruker.dittnav.eventaggregator.metrics.db

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get

fun Routing.cacheCountingApi(cacheEventCounterService: CacheEventCounterService) {

    get("/internal/cache/count/all") {
        val numberOfEvents = cacheEventCounterService.countAllEvents()
        call.respondText(text = numberOfEvents.toString(), contentType = ContentType.Text.Plain)
    }

    get("/internal/cache/count/beskjed") {
        val numberOfEvents = cacheEventCounterService.countBeskjeder()
        call.respondText(text = numberOfEvents.toString(), contentType = ContentType.Text.Plain)
    }

    get("/internal/cache/count/innboks") {
        val numberOfEvents = cacheEventCounterService.countInnboksEventer()
        call.respondText(text = numberOfEvents.toString(), contentType = ContentType.Text.Plain)
    }

    get("/internal/cache/count/oppgave") {
        val numberOfEvents = cacheEventCounterService.countOppgaver()
        call.respondText(text = numberOfEvents.toString(), contentType = ContentType.Text.Plain)
    }

    get("/internal/cache/count/done") {
        val numberOfEvents = cacheEventCounterService.countDoneEvents()
        call.respondText(text = numberOfEvents.toString(), contentType = ContentType.Text.Plain)
    }

    get("/internal/cache/count/done/all") {
        val numberOfEvents = cacheEventCounterService.countAllDoneEvents()
        call.respondText(text = numberOfEvents.toString(), contentType = ContentType.Text.Plain)
    }

}
