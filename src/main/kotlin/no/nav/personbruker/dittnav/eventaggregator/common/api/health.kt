package no.nav.personbruker.dittnav.eventaggregator.common.api

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import no.nav.personbruker.dittnav.eventaggregator.config.ApplicationContext

fun Routing.healthApi(appContext: ApplicationContext) {

    get("/isAlive") {
        call.respondText(text = "ALIVE", contentType = ContentType.Text.Plain)
    }

    get("/isReady") {
        if (isAllConsumersRunning(appContext) && isDataSourceRunning(appContext)) {
            call.respondText(text = "READY", contentType = ContentType.Text.Plain)
        } else {
            call.respondText(text = "NOTREADY", contentType = ContentType.Text.Plain, status = HttpStatusCode.FailedDependency)
        }
    }

}

private fun isAllConsumersRunning(appContext: ApplicationContext): Boolean {
    val allConsumersRunning =
            appContext.infoConsumer.isRunning() &&
            appContext.oppgaveConsumer.isRunning() &&
            appContext.meldingConsumer.isRunning()
    return allConsumersRunning
}

fun isDataSourceRunning(appContext: ApplicationContext): Boolean {
    return appContext.database.dataSource.isRunning
}
