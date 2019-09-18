package no.nav.personbruker.dittnav.eventaggregator.api

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import no.nav.personbruker.dittnav.eventaggregator.config.Server

fun Routing.healthApi() {

    get("/isAlive") {
        call.respondText(text = "ALIVE", contentType = ContentType.Text.Plain)
    }

    get("/isReady") {
        if (isAllConsumersRunning()) {
            call.respondText(text = "READY", contentType = ContentType.Text.Plain)
        } else {
            call.respondText(text = "NOTREADY", contentType = ContentType.Text.Plain, status = HttpStatusCode.FailedDependency)
        }
    }

}

private fun isAllConsumersRunning(): Boolean {
    val allConsumersRunning = Server.infoConsumer.isRunning() &&
            Server.oppgaveConsumer.isRunning() &&
            Server.meldingConsumer.isRunning()
    return allConsumersRunning
}
