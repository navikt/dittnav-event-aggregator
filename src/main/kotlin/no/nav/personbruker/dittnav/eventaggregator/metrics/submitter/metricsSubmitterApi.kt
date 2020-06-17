package no.nav.personbruker.dittnav.eventaggregator.metrics.submitter

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import no.nav.personbruker.dittnav.eventaggregator.config.ApplicationContext

fun Routing.metricsSubmitterApi(appContext: ApplicationContext) {

    get("/internal/metrics/submitter/start") {
        val responseText = "Starter periodisk innrapportering av metrikker."
        appContext.reinitializePeriodicMetricsSubmitter()
        appContext.periodicMetricsSubmitter.start()
        call.respondText(text = responseText, contentType = ContentType.Text.Plain)
    }

    get("/internal/metrics/submitter/stop") {
        val responseText = "Stoppet periodisk innrapportering av metrikker."
        appContext.periodicMetricsSubmitter.stop()
        call.respondText(text = responseText, contentType = ContentType.Text.Plain)
    }

    get("/internal/metrics/submit") {
        appContext.periodicMetricsSubmitter.submitMetrics()
        val responseText = "Det har blitt trigget rapportering av metrikker både for topics og cache."
        call.respondText(text = responseText, contentType = ContentType.Text.Plain)
    }

}
