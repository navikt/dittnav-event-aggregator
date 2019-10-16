package no.nav.personbruker.dittnav.eventaggregator.informasjon

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post

fun Routing.informasjonEventsApi(producer: InformasjonProducer) {

    post("/produce/informasjon") {
        producer.produceEvent()
        val msg = "Produced Informasjon-event"
        call.respond(HttpStatusCode.OK, msg)
    }

}
