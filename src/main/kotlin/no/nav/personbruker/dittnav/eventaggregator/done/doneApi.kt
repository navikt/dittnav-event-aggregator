package no.nav.personbruker.dittnav.eventaggregator.done

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post

fun Routing.doneEventsApi(producer: DoneProducer) {

    post("/produce/done") {
        producer.produceEvent()
        val msg = "Produced Done-event"
        call.respond(HttpStatusCode.OK, msg)
    }

}
