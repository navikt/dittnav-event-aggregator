package no.nav.personbruker.dittnav.eventaggregator.oppgave

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post

fun Routing.oppgaveEventsApi(producer: OppgaveProducer) {

    post("/produce/oppgave") {
        producer.produceEvent()
        val msg = "Produced Oppgave-event"
        call.respond(HttpStatusCode.OK, msg)
    }

}
