package no.nav.personbruker.dittnav.eventaggregator.melding

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post

fun Routing.meldingEventsApi(producer: MeldingProducer) {

    post("/produce/melding") {
        producer.produceEvent()
        val msg = "Produced Melding-event"
        call.respond(HttpStatusCode.OK, msg)
    }

}
