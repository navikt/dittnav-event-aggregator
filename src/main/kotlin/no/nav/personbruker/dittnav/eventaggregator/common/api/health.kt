package no.nav.personbruker.dittnav.eventaggregator.common.api

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
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

    get("/metrics") {
        val names = call.request.queryParameters.getAll("name")?.toSet() ?: emptySet()
        call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004), HttpStatusCode.OK) {
            TextFormat.write004(this, CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(names))
        }
    }

    get("/selftest") {
        var selftest = StringBuilder()
                .append("Informasjonconsumer running ${appContext.infoConsumer.isRunning()}\r\n")
                .append("Oppgaveconsumer running ${appContext.oppgaveConsumer.isRunning()}\r\n")
                .append("Innboksconsumer running ${appContext.innboksConsumer.isRunning()}\r\n")
                .append("Doneconsumer running ${appContext.doneConsumer.isRunning()}\r\n")
        call.respondText ( text = selftest.toString(), contentType = ContentType.Text.Plain)
    }
}

private fun isAllConsumersRunning(appContext: ApplicationContext): Boolean {
    val allConsumersRunning =
            appContext.infoConsumer.isRunning() &&
            appContext.oppgaveConsumer.isRunning() &&
            appContext.innboksConsumer.isRunning() &&
            appContext.doneConsumer.isRunning()
    return allConsumersRunning
}

fun isDataSourceRunning(appContext: ApplicationContext): Boolean {
    return appContext.database.dataSource.isRunning
}
