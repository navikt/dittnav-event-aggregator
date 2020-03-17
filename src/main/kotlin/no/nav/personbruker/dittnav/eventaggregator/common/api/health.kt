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
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.config.ApplicationContext

fun Routing.healthApi(appContext: ApplicationContext) {

    get("/internal/isAlive") {
        call.respondText(text = "ALIVE", contentType = ContentType.Text.Plain)
    }

    get("/internal/isReady") {
        if (isAllConsumersRunning(appContext) && isDataSourceRunning(appContext.database)) {
            call.respondText(text = "READY", contentType = ContentType.Text.Plain)
        } else {
            call.respondText(text = "NOTREADY", contentType = ContentType.Text.Plain, status = HttpStatusCode.FailedDependency)
        }
    }

    get("/internal/metrics") {
        val names = call.request.queryParameters.getAll("name")?.toSet() ?: emptySet()
        call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004), HttpStatusCode.OK) {
            TextFormat.write004(this, CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(names))
        }
    }

    get("/internal/selftest") {
        call.pingDependencies(appContext)
    }
}

private fun isAllConsumersRunning(appContext: ApplicationContext): Boolean {
    val allConsumersRunning =
            appContext.beskjedConsumer.isRunning() &&
            appContext.oppgaveConsumer.isRunning() &&
            appContext.innboksConsumer.isRunning() &&
            appContext.doneConsumer.isRunning()
    return allConsumersRunning
}

fun isDataSourceRunning(database: Database): Boolean {
    return database.dataSource.isRunning
}
