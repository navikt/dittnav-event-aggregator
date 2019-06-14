package no.nav.personbruker.dittnav.eventaggregator.api

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import no.nav.personbruker.dittnav.eventaggregator.Server

fun Routing.produceEventsApi() {

    post("/produce/informasjon") {
        Server.producer.produceInformasjonEvent()
        val msg = "Produced Informasjon-event"
        call.respond(HttpStatusCode.OK, msg)
    }

    post("/produce/oppgave") {
        Server.producer.produceOppgaveEvent()
        val msg = "Produced Oppgave-event"
        call.respond(HttpStatusCode.OK, msg)
    }

    post("/produce/melding") {
        Server.producer.produceMeldingEvent()
        val msg = "Produced Melding-event"
        call.respond(HttpStatusCode.OK, msg)
    }

}
