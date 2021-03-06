package no.nav.personbruker.dittnav.eventaggregator.common.kafka.polling

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import no.nav.personbruker.dittnav.eventaggregator.config.ApplicationContext
import no.nav.personbruker.dittnav.eventaggregator.config.KafkaConsumerSetup

fun Routing.pollingApi(appContext: ApplicationContext) {

    get("/internal/polling/start") {
        val responseText = "Polling etter eventer har blitt startet."
        KafkaConsumerSetup.restartPolling(appContext)
        call.respondText(text = responseText, contentType = ContentType.Text.Plain)
    }

    get("/internal/polling/stop") {
        val responseText = "All polling etter eventer har blitt stoppet."
        KafkaConsumerSetup.stopAllKafkaConsumers(appContext)
        call.respondText(text = responseText, contentType = ContentType.Text.Plain)
    }

    get("/internal/polling/checker/start") {
        val responseText = "Startet jobben som sjekker om konsumerne kjører."
        appContext.reinitializePeriodicConsumerPollingCheck()
        appContext.periodicConsumerPollingCheck.start()
        call.respondText(text = responseText, contentType = ContentType.Text.Plain)
    }

    get("/internal/polling/checker/stop") {
        val responseText = "Stoppet jobben som sjekker om konsumerne kjører."
        appContext.periodicConsumerPollingCheck.stop()
        call.respondText(text = responseText, contentType = ContentType.Text.Plain)
    }

}
